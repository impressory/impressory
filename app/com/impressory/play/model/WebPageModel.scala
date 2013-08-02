package com.impressory.play.model


import com.wbillingsley.handy._
import Ref._
import Permissions._
import play.api.libs.json._
import play.api.libs.ws.WS

/**
 *  
 */
object WebPageModel {

  def toJson(wp:WebPage) = Json.obj(
      "url" -> wp.url,
      "noFrame" -> wp.noFrame
  )
  
  def site(url: String) = {
    import java.net.{ URI, URISyntaxException }

    try {
      val uri = new URI(url);
      if (uri.isAbsolute) {
        val host = uri.getHost
        if (host.startsWith("www.")) {
          host.substring(4)
        } else host
      } else {
        "local"
      }
    } catch {
      case ex: URISyntaxException =>
        "(malformed url)"
    }
  }
  
  def updateWebPage(ce:ContentEntry, data:JsValue) = {
    ce.item match {
      case Some(wp:WebPage) => {
        wp.url = (data \ "item" \ "url").asOpt[String]
        ce.site = wp.url map { s => site(s) } getOrElse("(none)")
      } 
      case _ => { /* ignore */ }
    }
    ce.itself
  }
  
  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def create(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    val url = (data \ "item" \ "url").asOpt[String]
    ce.site = url map { s => site(s) } getOrElse("(none)")
    WebPage.unsaved(course, ce.itself, url)
  }  

  /**
   * Determines whether a piece of text is a URL or not
   */
  def urlMatcher(code:String) = {    
    val isUrl = code.startsWith("http://") || code.startsWith("https://")
    if (isUrl) {
      
      new ContentEntry(
          site = site(code),
          item = Some(new WebPage(url=Some(code)))  
        ).itself
    } else RefNone
  }  

}