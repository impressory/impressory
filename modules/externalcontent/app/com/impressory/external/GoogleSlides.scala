package com.impressory.external

import com.impressory.api._
import com.impressory.reactivemongo.ContentItemBsonHandler
import com.impressory.json.ContentItemJsonHandler
import com.impressory.plugins.ContentItemViewHandler
import com.wbillingsley.handy.{Approval, Ref}
import Ref._

case class GoogleSlides (
  var embedCode:Option[String] = None,
  var presId:Option[String] = None  
) extends ContentItem {
  
  val itemType = GoogleSlides.itemType
}


object GoogleSlides {
  
  val itemType = "Google Slides"

  /**
   * Views
   */
  object ViewHandler extends ContentItemViewHandler {
    def main = { case GoogleSlides.itemType => views.html.com.impressory.external.googleSlides.main().body } 
  
    def stream = PartialFunction.empty
  
    def edit = { case GoogleSlides.itemType => views.html.com.impressory.external.googleSlides.edit().body }  
  }
    
  /**
   * JSON handler for client communication
   */
  object JsonHandler extends ContentItemJsonHandler {

    import play.api.libs.json._
    
    implicit val format = Json.format[GoogleSlides]
    
    /**
     * Extracts the id from Google Slides URLs and embed codes
     */
    def extractGoogleSlidesId(url: String) = {
      "//docs.google.com/presentation/d/([^ \"?&/]+)".r.findFirstMatchIn(url).map(_.group(1))
    }

    /**
     * Determines whether a URL or embed code is a Google Slides presentation
     */
    def urlChecker(blank: ContentEntry, url: String) = {
      val presId = extractGoogleSlidesId(url)
      for (pid <- presId) yield {
        blank.copy(
          item = Some(GoogleSlides(embedCode = Some(url), presId = Some(pid))))
      }
    }

    def createFromJson= { case (GoogleSlides.itemType, json, blank) =>
      updateFromJson(
        GoogleSlides.itemType, json,
        blank.copy(settings=blank.settings.copy(published=Some(System.currentTimeMillis())))
      )
    }

    def updateFromJson = { case (GoogleSlides.itemType, json, before) =>
      val ec = (json \ "item" \ "embedCode").asOpt[String]
      val presId = for (str <- ec; id <- extractGoogleSlidesId(str)) yield id
      before.copy(
        tags = before.tags.copy(site = Some("google.com")),
        item = Some(GoogleSlides(
          embedCode = ec,
          presId = presId
        ))
      ).itself
    }

    def toJsonFor = { case (entry, gs: GoogleSlides, appr) => format.writes(gs).itself }
    
  }
    
  /**
   * BSON handler for database communication
   */
  object BsonHandler extends ContentItemBsonHandler {
    import reactivemongo.bson._
    val format = Macros.handler[GoogleSlides]
    
    def create = { case p:GoogleSlides => format.write(p) }
    
    def update = { case p:GoogleSlides => BSONDocument("item" -> format.write(p)) }
  
    def read = { case (GoogleSlides.itemType, doc) => format.read(doc) }    
  }

}