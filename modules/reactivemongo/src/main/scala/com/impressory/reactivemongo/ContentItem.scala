package com.impressory.reactivemongo

import reactivemongo.bson._
import reactivemongo.api._

import com.impressory.api._
import com.impressory.api.poll._
import com.impressory.api.qna._
import com.impressory.api.external._

object ContentItemConverter extends BSONDocumentWriter[ContentItem] {
  
  import MultipleChoicePoll._
  
  implicit val formatMCOpt = reactivemongo.bson.Macros.writer[MCOpt]
  
  import UserDAO.RefManyByIdWriter
  
  def write(i: ContentItem) = i match {
    case cs:ContentSequence => ContentSequenceWriter.write(cs) 
    case wp:WebPage => Macros.writer[WebPage].write(wp) 
    case gs:GoogleSlides => Macros.writer[GoogleSlides].write(gs)
    case y:YouTubeVideo => Macros.writer[YouTubeVideo].write(y)
    case mp:MarkdownPage => Macros.writer[MarkdownPage].write(mp)
    case mcp:MultipleChoicePoll => MultipleChoicePollWriter.write(mcp) 
    case _ => BSONDocument()
  }
  
  def read(doc:BSONDocument, kind:String):Option[ContentItem] = {
    kind match {
      case ContentSequence.itemType => Some(doc.as[ContentSequence](ContentSequenceReader))
      case WebPage.itemType => Some(doc.as[WebPage](Macros.reader[WebPage]))
      case GoogleSlides.itemType => Some(doc.as[GoogleSlides](Macros.reader[GoogleSlides]))
      case YouTubeVideo.itemType => Some(doc.as[YouTubeVideo](Macros.reader[YouTubeVideo]))
      case MarkdownPage.itemType => Some(doc.as[MarkdownPage](Macros.reader[MarkdownPage]))
      case MultipleChoicePoll.itemType => Some(doc.as[MultipleChoicePoll](MultipleChoicePollReader))
      case _ => None
    }
  }
  
  def update(i: ContentItem) = i match {
    case q:QnAQuestion => { "item.text" -> BSONString(q.text) }
    case _ => "item" -> write(i)
  }
}

