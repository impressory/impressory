package com.impressory.api.events

import com.impressory.api._
import com.wbillingsley.eventroom._
import com.wbillingsley.handy._

case class ChatStream(courseId:Id[Course,String]) extends ListenTo

