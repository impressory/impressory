package com.impressory.play.eventroom

import EventRoom._
import com.impressory.play.model._
import com.wbillingsley.eventroom._
import com.wbillingsley.handy.Ref._

object ChatEvents {
  
  case class ChatStream(courseId:String) extends ListenTo

  
  case class BroadcastIt(courseId:String, re:RecordedChatEvent) extends EREvent {
    override def toJson = {
      import com.impressory.play.json.JsonConverters._
      
      re.toJson
    }

    /**
     * The event room should just broadcast this to everyone who's listening
     */
    override def action(room:EventRoom) = {
      room.broadcast(ChatStream(courseId), this)
    }
  }
}