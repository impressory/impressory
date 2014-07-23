package com.impressory.json

import com.impressory.api._
import com.impressory.api.events._
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import play.api.libs.json._
import com.wbillingsley.handyplay.JsonConverter


object ChatCommentToJson extends JsonConverter[ChatComment, User] {
  
  def toJson(cc:ChatComment) = {
    Json.obj(
      "kind" -> "push", 
      "type" -> "chat",
      "id" -> cc.id,
      "course" -> cc.course,
      "created" -> cc.created,
      "text" -> cc.text,
      "topics" -> cc.topics,
      "addedBy" -> { if (cc.anonymous) JsNull else cc.addedBy }
    ).itself
  }
  
  def toJsonFor(cc:ChatComment, a:Approval[User]) = toJson(cc)

}