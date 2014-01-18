package com.impressory.external

import com.impressory.api._
import com.impressory.reactivemongo.ContentItemBsonHandler
import com.impressory.json.ContentItemJsonHandler
import com.impressory.plugins.ContentItemViewHandler
import com.wbillingsley.handy.{Ref, RefNone, Approval, RefFuture}
import Ref._

case class WebPage (
    
  var url:Option[String] = None,
  
  var imageUrl:Option[String] = None,
  
  var summary:Option[String] = None,

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
    def main = { case WebPage.itemType => views.html.com.impressory.external.webPage.main().body } 
  
    def stream = { case WebPage.itemType => views.html.com.impressory.external.webPage.stream().body }
  
    def edit = { case WebPage.itemType => views.html.com.impressory.external.webPage.edit().body }  
  }
    
  /**
   * JSON handler for client communication
   */
  object JsonHandler extends ContentItemJsonHandler {

    import play.api.libs.json._
    
    // Auto-generated JSON format
    val format = Json.format[WebPage]
    

    /**
     * Determines whether a piece of text is a URL or not
     */
    def urlChecker(blank: ContentEntry, url: String) = {
      val isUrl = url.startsWith("http://") || url.startsWith("https://")
    
      if (isUrl) {
        for (meta <- MetaExtractor.fetchAndExtract(url)) yield {
          blank.copy(
            title = meta.title,
            tags = CETags(site=meta.siteName),
            item = Some(new WebPage(
                url=meta.canonicalUrl,
                
                imageUrl = meta.imageUrl,
                
                summary = meta.summary,
                
                noFrame = meta.noFrame
            ))  
          )
        }
      } else RefNone
    }

    def createFromJson= { case (WebPage.itemType, json, blank) =>
      val url = (json \ "item" \ "url").asOpt[String]
      blank.setPublished(true)
      blank.copy(
        item = format.reads(json \ "item").asOpt
      ).itself
    }

    def updateFromJson = { case (WebPage.itemType, json, before) =>
      val url = (json \ "item" \ "url").asOpt[String]
      before.setPublished(true)
      before.copy(
        tags = before.tags.copy(site = url flatMap { s => MetaExtractor.site(s) }),
        item = format.reads(json \ "item").asOpt
      ).itself
    }

    def toJsonFor = { case (entry, wp: WebPage, appr) => 
      Json.writes[WebPage].writes(wp).itself
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