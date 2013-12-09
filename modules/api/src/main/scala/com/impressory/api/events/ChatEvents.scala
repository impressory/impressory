package com.impressory.api.events

import com.impressory.api._
import com.wbillingsley.eventroom._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.appbase.JsonConverter

case class ChatStream(courseId:String) extends ListenTo

case class BroadcastIt[T](courseId:String, cts:T)(implicit ttoj: JsonConverter[T, User]) extends EREvent {
  override def toJson = {
      ttoj.toJson(cts)
  }

    /**
     * The event room should just broadcast this to everyone who's listening
     */
  override def action(room:EventRoom) = {
    room.broadcast(ChatStream(courseId), this)
  }
}