package com.impressory.api.external

import com.wbillingsley.handy.{Approval, Ref, RefItself}
import Ref._
import com.impressory.api.{ContentItem, User}
import com.wbillingsley.handy.appbase.JsonConverter
import play.api.libs.json.Json

case class GoogleSlides (
  var embedCode:Option[String] = None,
  var presId:Option[String] = None  
) extends ContentItem {
  
  val itemType = GoogleSlides.itemType
  
  
}

object GoogleSlides extends JsonConverter[GoogleSlides, User] {
  
  val itemType = "Google Slides"
    
  val format = Json.format[GoogleSlides]
  
  def toJson(gs:GoogleSlides) = format.writes(gs).itself
  
  def toJsonFor(gs:GoogleSlides, u:Approval[User]) = format.writes(gs).itself
}

