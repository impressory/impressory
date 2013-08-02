package com.impressory.play.eventroom

import EventRoom._
import com.impressory.play.model._
import com.impressory.api._
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._
import scala.collection.mutable

import com.wbillingsley.eventroom

object MCPollEvents {

  /**
   * Gets or initialises the event room's poll state (the set of responses so far)
   */
  def getPollState(room: eventroom.EventRoom, pollId:String) = {
    room.states.get(PollStream(pollId)) match {
      case Some(p: PRState) => p.itself
      case _ => {
        import scala.collection.mutable
        
        /**
         * We need to sum up the responses so far
         */
        val poll = RefById(classOf[ContentEntry], pollId)
        val res = MCPollResponse.byPoll(poll).fold(mutable.Map.empty[Int, Int]){(rmap,pr) =>
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


  def broadcastState(room: eventroom.EventRoom, poll:Ref[ContentEntry]) {
    for (pollId <- poll.getId.map(_.stringify); state <- getPollState(room, pollId)) {
      room.broadcast(PollStream(pollId), state)
    }
  }

  case class PollStream(pollId: String) extends eventroom.ListenTo {
    override def onSubscribe(listenerName:String, room:eventroom.EventRoom) = broadcastState(room, RefById(classOf[ContentEntry], pollId))
  }

  case class PushPollToChat(poll: ContentEntry) extends eventroom.EREvent {
    override def toJson = {
      val course = poll.course.getId.map(_.stringify)
      val id = poll.id.stringify
      poll.item match {
        case Some(mc:MultipleChoicePoll) => {
          import com.impressory.play.json.JsonConverters._
          
          for (j <- poll.toJson) yield Json.obj(
            "kind" -> "push",
            "type" -> MultipleChoicePoll.itemType,
            "created" -> System.currentTimeMillis(),
            "poll" -> j
          )
        }
        case _ => {
          RefFailed(UserError(s"Content entry $id is not a multiple choice poll"))
        }
      }
    }

    /**
     * The event room should broadcast this to everyone who's listening to the book's chat stream
     */
    override def action(room: eventroom.EventRoom) {
      for (cid <- poll.course.getId) {
        room.broadcast(ChatEvents.ChatStream(cid.stringify), this)
      }
    }
  }

  /** A new poll response (vote) has been made. The vote contains the adjustment to the score for each option. */
  case class Vote(pollId:String, pr: Map[Int, Int]) extends eventroom.EREvent {

    /**
     * On receving this event, the room should re-tabulate the poll results and send them out to everyone
     * who's listening to the poll
     */
    override def action(room: eventroom.EventRoom) {
      for (state <- getPollState(room, pollId)) {
        for ((idx, diff) <- pr) {
          state.counts(idx) = state.counts.getOrElse(idx, 0) + diff
        }
        broadcastState(room, RefById(classOf[ContentEntry], pollId))
      }
    }

  }

  /** The state of a poll is the votes so far. Broadcast on each new vote. */
  case class PRState(val pollId: String, val counts: mutable.Map[Int, Int]) extends eventroom.State with eventroom.EREvent {
    override def toJson = Json.obj(
      "kind" -> "state",
      "type" -> "Multiple choice poll results",
      "id" -> pollId,
      "results" -> Json.toJson(
        counts.toSeq.map { case (opt, count) => Json.obj("option" -> opt, "votes" -> count) }
      )
    ).itself    
  }

}