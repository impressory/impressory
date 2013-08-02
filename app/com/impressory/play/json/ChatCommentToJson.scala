package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._

import JsonConverters._


object ChatCommentToJson {
  
  def toJson(cc:ChatComment) = {
    Json.obj(
      "kind" -> "push", 
      "type" -> "chat",
      "id" -> cc._id,
      "course" -> cc.course,
      "created" -> cc.created,
      "text" -> cc.text,
      "topics" -> cc.topics,
      "addedBy" -> { if (cc.anonymous) RefNone else cc.addedBy }
    ) 
  }

}