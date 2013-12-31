package com.impressory.external

import com.impressory.api._
import com.impressory.reactivemongo.ContentItemBsonHandler
import com.impressory.json.ContentItemJsonHandler
import com.impressory.plugins.ContentItemViewHandler
import com.wbillingsley.handy.{Ref, RefNone, Approval, RefFuture}
import Ref._

case class WebPage (
    
  var url:Option[String] = None,

  var noFrame:Boolean = false
  
) extends ContentItem {
  
  val itemType = WebPage.itemType
  
}

object WebPage {
  
  val itemType = "web page"
  

  /**
   * Views
   */
  object ViewHandler extends ContentItemViewHandler {
    def main = { case "webPage.html" => views.html.com.impressory.external.webPage.main().body } 
  
    def stream = PartialFunction.empty
  
    def edit = { case "webPage.html" => views.html.com.impressory.external.webPage.edit().body }  
  }
    
  /**
   * JSON handler for client communication
   */
  object JsonHandler extends ContentItemJsonHandler {

    import play.api.libs.json._
    
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
        case ex: URISyntaxException => Some("(malformed url)")
      }
    }
    
    /**
     * Determines whether a piece of text is a URL or not
     */
    def urlChecker(blank: ContentEntry, url: String) = {
      val isUrl = url.startsWith("http://") || url.startsWith("https://")
    
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

    def createFromJson= { case (WebPage.itemType, json, blank) =>
      val url = (json \ "item" \ "url").asOpt[String]
      blank.setPublished(true)
      blank.copy(
        tags = blank.tags.copy(site = url flatMap { s => site(s) }),
        item = Some(WebPage(
          url = url
        ))
      ).itself
    }

    def updateFromJson = { case (WebPage.itemType, json, before) =>
      val url = (json \ "item" \ "url").asOpt[String]
      before.setPublished(true)
      before.copy(
        tags = before.tags.copy(site = url flatMap { s => site(s) }),
        item = Some(WebPage(
          url = url
        ))
      ).itself
    }

    def toJsonFor = { case (entry, wp: WebPage, appr) => 
      Json.obj(
        "url" -> wp.url,
        "noFrame" -> wp.noFrame
      ).itself
    }
  }
    
  /**
   * BSON handler for database communication
   */
  object BsonHandler extends ContentItemBsonHandler {
    
    import reactivemongo.bson._
    
    val format = Macros.handler[WebPage]
    
    def create = { case p:WebPage => format.write(p) }
    
    def update = { case p:WebPage => BSONDocument("item" -> format.write(p)) }
  
    def read = { case (WebPage.itemType, doc) => format.read(doc) }    
  }

}