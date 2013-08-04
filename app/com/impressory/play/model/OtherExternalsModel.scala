package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import Permissions._
import play.api.libs.json._

object OtherExternalsModel {
  
  implicit val GoogleSlidesToJson = Json.format[GoogleSlides]
  implicit val YouTubeVideoToJson = Json.format[YouTubeVideo]
  
  def createGoogleSlides(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    ce.tags.site = Some("google.com")
    ce.setPublished(true)    
    updateGoogleSlides(new GoogleSlides(), data)
  } 
  
  def updateGoogleSlides(gs:GoogleSlides, data:JsValue) = {
    val ec = (data \ "item" \ "embedCode").asOpt[String]
    
    val presId = for (str <- ec; id <- extractGoogleSlidesId(str)) yield id
    gs.embedCode = ec
    gs.presId = presId
    gs
  }
  
  /**
   * Extracts the id from Google Slides URLs and embed codes
   */
  def extractGoogleSlidesId(url:String) = {
    "//docs.google.com/presentation/d/([^ \"?&/]+)".r.findFirstMatchIn(url).map(_.group(1))
  }
  
  /**
   * Determines whether a URL or embed code is a Google Slides presentation
   */
  def googleSlidesMatcher(code:String) = {
    val presId = extractGoogleSlidesId(code)
    for (pid <- presId) yield {
      new ContentEntry(
        item = Some(GoogleSlides(embedCode=Some(code), presId=Some(pid)))
      )
    }
  }
  
  
  def createYouTubeVideo(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    ce.tags.site = Some("google.com")
    ce.setPublished(true)    
    updateYouTubeVideo(new YouTubeVideo(), data)
  } 
  
  def updateYouTubeVideo(y:YouTubeVideo, data:JsValue) = {
    val ec = (data \ "item" \ "embedCode").asOpt[String]
    
    val presId = for (str <- ec; id <- extractYouTubeId(str)) yield id
    y.embedCode = ec
    y.videoId = presId
    y
  }  
    
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
  def youTubeMatcher(code:String) = {
    val videoId = extractYouTubeId(code)
    for (vid <- videoId) yield {
      new ContentEntry(
        item = Some(YouTubeVideo(embedCode=Some(code), videoId=Some(vid)))
      )
    }
  }

}