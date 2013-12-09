package com.impressory.json

import com.wbillingsley.handy.{Ref, RefNone, RefFailed, Approval}
import Ref._
import com.impressory.api._
import com.impressory.api.external._
import play.api.libs.json._
import com.wbillingsley.handy.appbase.JsonConverter

object ContentItemToJson extends JsonConverter[ContentItem, User] {
  
  var toJsonPF:PartialFunction[(ContentItem, Approval[User]), Ref[JsObject]] = {
    case (gs:GoogleSlides, _) => Json.writes[GoogleSlides].writes(gs).itself
    case (yt:YouTubeVideo, _) => Json.writes[YouTubeVideo].writes(yt).itself
  }
  
  var createFromJsonPF:PartialFunction[(String, ContentEntry, JsValue), Ref[ContentEntry]] = PartialFunction.empty

  var updateFromJsonPF:PartialFunction[(ContentEntry, JsValue), Ref[ContentEntry]] = PartialFunction.empty

  
  def toJsonFor(ci:ContentItem, appr:Approval[User]):Ref[JsObject] = toJsonPF(ci, appr)

  def toJson(ci:ContentItem) = toJsonFor(ci, Approval(RefNone))
  
  def createFromJson(kind:String, blank:ContentEntry, json:JsValue):Ref[ContentEntry] = {
    createFromJsonPF(kind, blank, json)
  }
  
  def updateFromJson(before:ContentEntry, json:JsValue):Ref[ContentEntry] = {
    updateFromJsonPF(before, json)
  }
  
}