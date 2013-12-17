package com.impressory.api.events

import com.impressory.api._
import com.wbillingsley.eventroom._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.appbase.JsonConverter

case class ChatStream(courseId:String) extends ListenTo

