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
  
  def site(url: String):Option[String] = {
    import java.net.{ URI, URISyntaxException }

    try {
      val uri = new URI(url);
      if (uri.isAbsolute) {
        val host = uri.getHost
        if (host.startsWith("www.")) {
          Some(host.substring(4))
        } else Some(host)
      } else {
        None
      }
    } catch {
      case ex: URISyntaxException =>
        Some("(malformed url)")
    }
  }
  
  def updateWebPage(ce:ContentEntry, data:JsValue) = {
    ce.item match {
      case Some(wp:WebPage) => {
        wp.url = (data \ "item" \ "url").asOpt[String]
        ce.tags.site = wp.url flatMap { s => site(s) } 
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
    ce.tags.site = url flatMap { s => site(s) } 
    WebPage.unsaved(course, ce.itself, url)
  }  

  /**
   * Determines whether a piece of text is a URL or not
   */
  def urlMatcher(code:String) = {    
    val isUrl = code.startsWith("http://") || code.startsWith("https://")
    
    println(s"Checking to see if ${code} is a url")
    
    if (isUrl) {
      import play.api.libs.ws.WS
      import play.api.libs.concurrent.Execution.Implicits._
      
      val a = for (
        res <- WS.url(code).withHeaders("Accept" -> "text/*").get();
        title = "<title>([^<]+)</title>".r.findFirstMatchIn(res.body).map(_.group(1))
      ) yield {
        new ContentEntry(
          title = title,
          tags = CETags(site=site(code)),
          item = Some(new WebPage(url=Some(code)))  
        )
      }
      
      new RefFuture(a)
    } else RefNone
  }  

}