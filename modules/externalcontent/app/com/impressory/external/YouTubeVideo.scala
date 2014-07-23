package com.impressory.external

import com.impressory.api._
import com.impressory.reactivemongo.ContentItemBsonHandler
import com.impressory.json.ContentItemJsonHandler
import com.impressory.plugins.ContentItemViewHandler
import com.wbillingsley.handy.{Approval, Ref}
import Ref._

case class YouTubeVideo (
  var embedCode:Option[String] = None,
  var videoId:Option[String] = None,

  var title:Option[String] = None,

  var url:Option[String] = None,
  
  var imageUrl:Option[String] = None,
  
  var summary:Option[String] = None
) extends ContentItem {
  
  val itemType = YouTubeVideo.itemType
  
}

object YouTubeVideo {
  
  val itemType = "YouTube video"
  
  /**
   * Views
   */
  object ViewHandler extends ContentItemViewHandler {
    def main = { case YouTubeVideo.itemType => views.html.com.impressory.external.youtubeVideo.main().body } 
  
    def stream = { case YouTubeVideo.itemType => views.html.com.impressory.external.youtubeVideo.stream().body }  
  
    def edit = { case YouTubeVideo.itemType => views.html.com.impressory.external.youtubeVideo.edit().body }  
  }
    
  /**
   * JSON handler for client communication
   */
  object JsonHandler extends ContentItemJsonHandler {

    import play.api.libs.json._

    implicit val format = Json.format[YouTubeVideo]

    /**
     * Extracts the id from YouTube URLs and embed codes
     */
    def extractYouTubeId(url: String) = {
      "//www.youtube.com/watch\\?v=([^ \"?&]+)".r.findFirstMatchIn(url).map(_.group(1)) orElse
        "//youtu.be/([^ \"?&]+)".r.findFirstMatchIn(url).map(_.group(1)) orElse
        "//www.youtube.com/embed/([^ \"?&]+)".r.findFirstMatchIn(url).map(_.group(1))
    }

    /**
     * Determines whether a URL or embed code is a YouTube video
     */
    def urlChecker(blank: ContentEntry, url: String) = {
      val videoId = extractYouTubeId(url).toRef
      for {
        vid <- videoId
        
        // If we just fetch and extract data from the web page version, we don't have to be signed up to Google APIs
        meta <- MetaExtractor.fetchAndExtract(s"http://www.youtube.com/watch?v=${vid}")
      } yield {
        val title = meta.title
        
        blank.copy(
          message = blank.message.copy(title=title),
          tags = CETags(site=meta.siteName),
          item = Some(new YouTubeVideo(
              embedCode = Some(url), 
              
              videoId = Some(vid),
              
              title = title,
              
              url=meta.canonicalUrl,
              
              imageUrl = meta.imageUrl,
              
              summary = meta.summary
          )))
      }
    }

    def createFromJson= { case (YouTubeVideo.itemType, json, blank) =>
      updateFromJson(
        YouTubeVideo.itemType, json,
        blank.copy(
          settings=blank.settings.copy(published=Some(System.currentTimeMillis()))
        )
      )
    }

    def updateFromJson = {
      case (YouTubeVideo.itemType, json, before) =>
        val ec = (json \ "item" \ "embedCode").asOpt[String]
        val presId = for (str <- ec; id <- extractYouTubeId(str)) yield id
        before.copy(
          item = format.reads(json \ "item").asOpt
        ).itself
    }

    def toJsonFor = { case (entry, gs: YouTubeVideo, appr) => format.writes(gs).itself }
    
  }
    
  /**
   * BSON handler for database communication
   */
  object BsonHandler extends ContentItemBsonHandler {
    import reactivemongo.bson._
    val format = Macros.handler[YouTubeVideo]
    
    def create = { case p:YouTubeVideo => format.write(p)  }
    
    def update = { case p:YouTubeVideo => BSONDocument("item" -> format.write(p)) }
  
    def read = { case (YouTubeVideo.itemType, doc) => format.read(doc) }    
  }    
}