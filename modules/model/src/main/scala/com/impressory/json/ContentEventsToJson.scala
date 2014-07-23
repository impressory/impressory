package com.impressory.json

import com.impressory.api._
import com.impressory.api.events._
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import play.api.libs.json._
import com.wbillingsley.handyplay.JsonConverter

object ContentEditedToJson extends JsonConverter[ContentEdited, User]  {
    
  def toJson(evt:ContentEdited) = {
    for (ce <- ContentEntryToJson.toJson(evt.ce)) yield Json.obj(
      "kind" -> "state", 
      "type" -> "content entry edited",
      "entry" -> ce
    )
  }
  
  def toJsonFor(evt:ContentEdited, appr:Approval[User]) = {
    for (ce <- ContentEntryToJson.toJsonFor(evt.ce, appr)) yield Json.obj(
      "kind" -> "state", 
      "type" -> "content entry edited",
      "entry" -> ce
    )
  }  
}
 
object ContentPublishedToJson extends JsonConverter[ContentPublished, User]  {
  
  def toJson(evt:ContentPublished) = {
    for (ce <- ContentEntryToJson.toJson(evt.ce)) yield Json.obj(
      "kind" -> "push", 
      "type" -> "content entry published",
      "entry" -> ce
    )    
  }

  def toJsonFor(evt:ContentPublished, appr:Approval[User]) = {
    for (ce <- ContentEntryToJson.toJsonFor(evt.ce, appr)) yield Json.obj(
      "kind" -> "push", 
      "type" -> "content entry published",
      "entry" -> ce
    )    
  }
}