package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import Permissions._
import play.api.libs.json._

object OtherExternalsModel {
  

  def createGoogleSlides(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    ce.site = "google.com"
    updateGoogleSlides(new GoogleSlides(), data)
  } 
  
  def updateGoogleSlides(gs:GoogleSlides, data:JsValue) = {
    val ec = (data \ "item" \ "embedCode").asOpt[String]
    
    val presId = for (str <- ec) yield {
      val codeMatcher = "presentation/d/([^ \"]+)/embed".r
      
      codeMatcher.findFirstMatchIn(str) match {
        case Some(regex) => regex.group(1)
        case None => str
      }
    }
    gs.embedCode = ec
    gs.presId = presId
    gs
  }
  
  def createYouTubeVideo(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    ce.site = "google.com"
    updateYouTubeVideo(new YouTubeVideo(), data)
  } 
  
  def updateYouTubeVideo(y:YouTubeVideo, data:JsValue) = {
    val ec = (data \ "item" \ "embedCode").asOpt[String]
    
    val presId = for (str <- ec) yield {
      // TODO: Embed code matcher for YouTube
      val codeMatcher = "presentation/d/([^ \"]+)/embed".r
      
      codeMatcher.findFirstMatchIn(str) match {
        case Some(regex) => regex.group(1)
        case None => str
      }
    }
    y.embedCode = ec
    y.videoId = presId
    y
  }  

}