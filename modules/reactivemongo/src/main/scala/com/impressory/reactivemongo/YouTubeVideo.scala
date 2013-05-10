package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import com.impressory.api._

import play.api.libs.concurrent.Execution.Implicits._

case class YouTubeVideo (
  var embedCode:Option[String] = None,
  var videoId:Option[String] = None  
) extends ContentItem {
  
  val itemType = YouTubeVideo.itemType
  
}

object YouTubeVideo {
  
  val itemType = "YouTube video"
  
  implicit val format = Macros.handler[YouTubeVideo]
  
}