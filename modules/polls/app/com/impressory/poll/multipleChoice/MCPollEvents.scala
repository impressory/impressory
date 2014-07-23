package com.impressory.poll.multipleChoice

import com.impressory.api._
import com.impressory.api.events._
import com.impressory.poll._
import com.impressory.json._
import com.impressory.security._
import com.impressory.eventroom._
import EventRoom._

import com.wbillingsley.handy._
import Ref._
import Id._

import play.api.libs.json._
import scala.collection.mutable
import com.wbillingsley.eventroom

// Import the currently configured LookUps
import com.impressory.plugins.LookUps._

object MCPollEventHelper {
  
  /**
   * Gets or initialises the event room's poll state (the set of responses so far)
   */
  def getPollState(room: eventroom.EventRoom, pollId:Id[ContentEntry,String]) = {
    room.states.get(PollStream(pollId)) match {
      case Some(p: PRState) => p.itself
      case _ => {
        import scala.collection.mutable
        
        /**
         * We need to sum up the responses so far
         */
        val poll = pollId.lazily
        val res = MCPollResponseDAO.byPoll(poll).fold(mutable.Map.empty[Int, Int]){(rmap,pr) =>
          for (ans <- pr.answer) {
            rmap(ans) = rmap.getOrElse(ans, 0) + 1
          }
          rmap
        }
        for (map <- res) yield {
          val state = PRState(pollId, map)
          room.states = room.states.updated(PollStream(pollId), state)
          state
        }
      }
    }
  }


  def broadcastState(room: eventroom.EventRoom, pollId:Id[ContentEntry,String]) {
    for (state <- getPollState(room, pollId)) {
      room.broadcast(PollStream(pollId), state)
    }
  }
}

import MCPollEventHelper._

case class PollStream(pollId: Id[ContentEntry,String]) extends eventroom.ListenTo {
    override def onSubscribe(listenerName:String, room:eventroom.EventRoom) = broadcastState(room, pollId)
}

object MCPollStreamLTJH extends ListenToJsonHandler {
  def fromJson = { case ("Multiple choice poll results", j, appr) =>
    for {
      pollIdStr <- (j \ "id").asOpt[String].toRef
      pollId = pollIdStr.asId[ContentEntry]
      approved <- appr ask Permissions.readEntry(pollId.lazily)
    } yield PollStream(pollId)
    
  }
}

case class PushPollToChat(poll: ContentEntry) extends com.wbillingsley.eventroom.JsonEvent(
  ChatStream(poll.course),
  Json.obj(
    "kind" -> "push",
    "type" -> MultipleChoicePoll.itemType,
    "created" -> System.currentTimeMillis(),
    "poll" -> poll.id
  )
)

/** A new poll response (vote) has been made. The vote contains the adjustment to the score for each option. */
case class Vote(pollId:Id[ContentEntry,String], pr: Map[Int, Int]) extends eventroom.EREvent {
    /**
     * On receving this event, the room should re-tabulate the poll results and send them out to everyone
     * who's listening to the poll
     */
    override def action(room: eventroom.EventRoom) {
      for (state <- getPollState(room, pollId)) {
        for ((idx, diff) <- pr) {
          state.counts(idx) = state.counts.getOrElse(idx, 0) + diff
        }
        broadcastState(room, pollId)
      }
    }
}

/** The state of a poll is the votes so far. Broadcast on each new vote. */
case class PRState(val pollId: Id[ContentEntry,String], val counts: mutable.Map[Int, Int]) extends eventroom.State with eventroom.EREvent {
    override def toJson = Json.obj(
      "kind" -> "state",
      "type" -> "Multiple choice poll results",
      "id" -> pollId,
      "results" -> Json.toJson(
        counts.toSeq.map { case (opt, count) => Json.obj("option" -> opt, "votes" -> count) }
      )
    ).itself    
}
