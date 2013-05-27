package com.impressory.reactivemongo

import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}

trait ContentItem {
  val itemType:String
}

object ContentItem {
  
  implicit object bsonWriter extends BSONDocumentWriter[ContentItem] {
    def write(i: ContentItem) = i match {
      case cs:ContentSequence => ContentSequence.bsonWriter.write(cs) 
      case wp:WebPage => WebPage.bsonWriter.write(wp) 
      case gs:GoogleSlides => GoogleSlides.format.write(gs)
      case y:YouTubeVideo => YouTubeVideo.format.write(y)
      case mp:MarkdownPage => MarkdownPage.bsonWriter.write(mp)
      case mcp:MultipleChoicePoll => MultipleChoicePoll.bsonWriter.write(mcp) 
      case _ => BSONDocument()
    }
  }
}
