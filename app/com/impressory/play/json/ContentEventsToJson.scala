package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._

import JsonConverters._

import com.impressory.play.eventroom.ContentEvents._

object ContentEventsToJson {
    
  def contentEditedToJson(ce:ContentEdited) = {
    for (ce <- ce.ce.toJson) yield Json.obj(
      "kind" -> "state", 
      "type" -> "content entry edited",
      "entry" -> ce
    )
  }
  
  def contentPublishedToJson(ca:ContentPublished) = {
    for (ce <- ca.ce.toJson) yield Json.obj(
      "kind" -> "push", 
      "type" -> "content entry published",
      "entry" -> ce
    )    
  }

}