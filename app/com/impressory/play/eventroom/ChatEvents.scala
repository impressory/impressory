package com.impressory.play.eventroom

import EventRoom._
import com.impressory.play.model._
import com.wbillingsley.eventroom._
import play.api.libs.json._
import com.wbillingsley.handy.Ref._

object ChatEvents {
  
  case class ChatStream(courseId:String) extends ListenTo

  
  case class BroadcastIt(courseId:String, re:RecordedChatEvent) extends EREvent {
    override def toJson = {
      import com.impressory.play.json.JsonConverters._
      
      Json.toJson(re).itself
    }

    /**
     * The event room should just broadcast this to everyone who's listening
     */
    override def action(room:EventRoom) = {
      room.broadcast(ChatStream(courseId), this)
    }
  }
  
  case class ChatMessage(anonymous: Boolean, course: String, mem: Mem, text: Option[String], topics: Seq[String]) extends EREvent {
    
    implicit val writes = Json.writes[ChatMessage]
    
    override def toJson = {
      Json.toJson(this).itself
    }
    
    /**
     * The event room should just broadcast this to everyone who's listening
     */
    override def action(room:EventRoom) = {
      room.broadcast(ChatStream(course), this)
    }
  }

}