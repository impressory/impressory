package com.impressory.json

import com.wbillingsley.handy.{Ref, RefNone, RefFailed, Approval}
import Ref._
import com.impressory.api._
import play.api.libs.json._
import com.wbillingsley.handyplay.JsonConverter

object ContentItemToJson {
    
  def toJsonFor(entry:ContentEntry, ci:ContentItem, appr:Approval[User]):Ref[JsObject] = {
    for {
      h <- handlers.find(_.toJsonFor.isDefinedAt((entry, ci, appr))).toRef orIfNone RefFailed(new IllegalStateException(s"No toJsonFor handler for ${ci.itemType} has been registered"))
      j <- h.toJsonFor(entry, ci, appr)
    } yield j
  }

  def toJson(entry:ContentEntry, ci:ContentItem) = toJsonFor(entry, ci, Approval(RefNone))
  
  def createFromJson(kind:String, blank:ContentEntry, json:JsValue):Ref[ContentEntry] = {
    for {
      h <- handlers.find(_.createFromJson.isDefinedAt((kind, json, blank))).toRef orIfNone RefFailed(new IllegalStateException(s"No create handler for ${kind} has been registered"))
      r <- h.createFromJson(kind, json, blank)
    } yield r
  }
  
  def updateFromJson(before:ContentEntry, json:JsValue):Ref[ContentEntry] = {
    for {
      kind <- before.kind.toRef orIfNone RefFailed(new IllegalStateException("Content entry had no kind"))
      h <- handlers.find(_.updateFromJson.isDefinedAt((kind, json, before))).toRef orIfNone RefFailed(new IllegalStateException(s"No update handler for ${kind} has been registered"))
      r <- h.updateFromJson(kind, json, before)
    } yield r
  }
  
  
  def whatIsIt(blank:ContentEntry, url:String) = {
    handlers.foldLeft[Ref[ContentEntry]](RefNone)(_ orIfNone _.urlChecker(blank, url))
  }

  private var handlers:Seq[ContentItemJsonHandler] = Seq.empty
  
  def registerHandler(jh:ContentItemJsonHandler) = this.synchronized {
    handlers = handlers :+ jh
  }  
  
}

trait ContentItemJsonHandler {
  
  def urlChecker(blank:ContentEntry, url:String):Ref[ContentEntry]
  
  def createFromJson:PartialFunction[(String, JsValue, ContentEntry), Ref[ContentEntry]]
  
  def updateFromJson:PartialFunction[(String, JsValue, ContentEntry), Ref[ContentEntry]]
  
  /**
   * Produces a JSON block representing the content item. The content entry is also passed in,
   * as the function may need to look things up (eg, using the content entry's ID or course)
   */
  def toJsonFor:PartialFunction[(ContentEntry, ContentItem, Approval[User]), Ref[JsObject]]

}
