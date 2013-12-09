package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._

import play.api.libs.json._

import com.impressory.api._
import com.impressory.plugins._
import com.impressory.api.external._

object GoogleSlidesModel extends ContentItemJsonHandler[GoogleSlides] {
  
  implicit val format = Json.format[GoogleSlides]
  
  val clazz = classOf[GoogleSlides]
  
  val kind = GoogleSlides.itemType
  
  /**
   * Extracts the id from Google Slides URLs and embed codes
   */
  def extractGoogleSlidesId(url:String) = {
    "//docs.google.com/presentation/d/([^ \"?&/]+)".r.findFirstMatchIn(url).map(_.group(1))
  }

  /**
   * Determines whether a URL or embed code is a Google Slides presentation
   */
  def urlChecker(blank:ContentEntry, url:String) = {
    val presId = extractGoogleSlidesId(url)
    for (pid <- presId) yield {
      blank.copy(
        item = Some(GoogleSlides(embedCode=Some(url), presId=Some(pid)))
      )
    }
  }
  
  def createFromJson(blank:ContentEntry, json:JsValue) = {
    updateFromJson(blank, json)
  } 
  
  def updateFromJson(before:ContentEntry, json:JsValue) = {
    val ec = (json \ "item" \ "embedCode").asOpt[String]
    val presId = for (str <- ec; id <- extractGoogleSlidesId(str)) yield id
    before.copy(
      tags = before.tags.copy(site=Some("google.com")),
      item = Some(GoogleSlides(
        embedCode = ec,
        presId = presId
      ))
    ).itself
  }
  
  def toJsonFor(gs:GoogleSlides, appr:Approval[User]) = format.writes(gs).itself

}


object YouTubeVideoModel extends ContentItemJsonHandler[YouTubeVideo] {
  
  implicit val format = Json.format[YouTubeVideo]
    
  val clazz = classOf[YouTubeVideo]
  
  val kind = YouTubeVideo.itemType  
  
  /**
   * Extracts the id from YouTube URLs and embed codes
   */
  def extractYouTubeId(url:String) = {
    "//www.youtube.com/watch\\?v=([^ \"?&]+)".r.findFirstMatchIn(url).map(_.group(1)) orElse
    "//youtu.be/([^ \"?&]+)".r.findFirstMatchIn(url).map(_.group(1)) orElse
    "//www.youtube.com/embed/([^ \"?&]+)".r.findFirstMatchIn(url).map(_.group(1))       
  }
  
  /**
   * Determines whether a URL or embed code is a YouTube video
   */
  def urlChecker(blank:ContentEntry, url:String) = {
    val videoId = extractYouTubeId(url)
    for (vid <- videoId) yield {
      blank.copy(
        item = Some(YouTubeVideo(embedCode=Some(url), videoId=Some(vid)))
      )
    }
  }

  
  def createFromJson(blank:ContentEntry, json:JsValue) = {
    updateFromJson(blank, json)
  } 
  
  def updateFromJson(before:ContentEntry, json:JsValue) = {
    val ec = (json \ "item" \ "embedCode").asOpt[String]
    val presId = for (str <- ec; id <- extractYouTubeId(str)) yield id
    before.copy(
      tags = before.tags.copy(site=Some("youtube.com")),
      item = Some(YouTubeVideo(
        embedCode = ec,
        videoId = presId
      ))
    ).itself
  }
  
  def toJsonFor(yt:YouTubeVideo, appr:Approval[User]) = format.writes(yt).itself
}