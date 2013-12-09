package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._
import play.api.libs.ws.WS
import com.impressory.api._
import com.impressory.api.external._
import com.impressory.plugins._
import com.impressory.security.Permissions
import Permissions._

/**
 *  
 */
object WebPageModel extends ContentItemJsonHandler[WebPage] {

  val clazz = classOf[WebPage]
  
  val kind = WebPage.itemType
  
  def toJsonFor(wp:WebPage, appr:Approval[User]) = Json.obj(
      "url" -> wp.url,
      "noFrame" -> wp.noFrame
  ).itself
  
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
  
  def updateFromJson(before:ContentEntry, json:JsValue) = {
    val url = (json \ "item" \ "url").asOpt[String]
    before.setPublished(true)
    before.copy(
      tags = before.tags.copy(site = url flatMap { s => site(s) }),
      item = Some(WebPage(
        url = url
      ))
    ).itself
  }
  
  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def createFromJson(blank:ContentEntry, json:JsValue) = {
    val url = (json \ "item" \ "url").asOpt[String]
    blank.setPublished(true)
    blank.copy(
      tags = blank.tags.copy(site = url flatMap { s => site(s) }),
      item = Some(WebPage(
        url = url
      ))
    ).itself
  }  

  /**
   * Determines whether a piece of text is a URL or not
   */
  def urlChecker(blank:ContentEntry, url:String) = {
    val isUrl = url.startsWith("http://") || url.startsWith("https://")
    
    println(s"Checking to see if ${url} is a url")
    
    if (isUrl) {
      import play.api.libs.ws.WS
      import play.api.libs.concurrent.Execution.Implicits._
      
      val a = for (
        res <- WS.url(url).withHeaders("Accept" -> "text/*").get();
        title = "<title>([^<]+)</title>".r.findFirstMatchIn(res.body).map(_.group(1))
      ) yield {
        blank.copy(
          title = title,
          tags = CETags(site=site(url)),
          item = Some(new WebPage(url=Some(url)))  
        )
      }
      
      new RefFuture(a)
    } else RefNone
  }  

}