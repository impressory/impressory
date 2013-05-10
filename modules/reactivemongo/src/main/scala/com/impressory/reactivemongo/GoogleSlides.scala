package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import com.impressory.api._

import play.api.libs.concurrent.Execution.Implicits._

case class GoogleSlides (
  var embedCode:Option[String] = None,
  var presId:Option[String] = None  
) extends ContentItem {
  
  val itemType = GoogleSlides.itemType
  
}

object GoogleSlides {
  
  val itemType = "Google Slides"
  
  implicit val format = Macros.handler[GoogleSlides]
  
}