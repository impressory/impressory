package com.impressory.api.events

import com.wbillingsley.handy._
import com.impressory.api._

trait RecordedChatEvent {
  /**
   * When we store more than one kind of chat event, this will be needed to determine how to deserialise an event
   */
  val eventType: String
}

case class ChatComment (
  
  id:Id[ChatComment,String],
    
  text:String,

  anonymous:Boolean = true,

  course:Id[Course,String],

  addedBy:Option[Id[User,String]] = None,

  session:Option[String] = None,

  topics:Set[String] = Set.empty[String],
    
  created: Long = System.currentTimeMillis
    
) extends HasStringId[ChatComment] with RecordedChatEvent {
  
  /**
   * Two entries are equal if they have the same ID
   */
  override def equals(obj: Any) = {
    obj.isInstanceOf[ChatComment] &&
      obj.asInstanceOf[ChatComment].id == id
  }
    
  val eventType = "chatComment"
  
}