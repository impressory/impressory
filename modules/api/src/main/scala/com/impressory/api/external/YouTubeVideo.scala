package com.impressory.api.external

import com.impressory.api.ContentItem

case class YouTubeVideo (
  var embedCode:Option[String] = None,
  var videoId:Option[String] = None  
) extends ContentItem {
  
  val itemType = YouTubeVideo.itemType
  
}

object YouTubeVideo {
  
  val itemType = "YouTube video"
  
}