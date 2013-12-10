package com.impressory.reactivemongo

import reactivemongo.bson._
import reactivemongo.api._

import com.impressory.api._
import com.impressory.api.poll._
import com.impressory.api.qna._

object ContentItemToBson {
  
  
  def owrite(i: ContentItem) = {
    i match {
      case mcp:MultipleChoicePoll => MultipleChoicePollWriter.write(mcp) 
      case _ => BSONDocument()
    }
  }
  
  def oread(doc:BSONDocument, kind:String):Option[ContentItem] = {
    kind match {
      case MultipleChoicePoll.itemType => Some(doc.as[MultipleChoicePoll](MultipleChoicePollReader))
      case _ => None
    }
  }
  
  def oupdate(i: ContentItem) = i match {
    case q:QnAQuestion => { "item.text" -> BSONString(q.text) }
    case _ => "item" -> owrite(i)
  }
  
  /**
   * The list of handlers for different kinds of content item. 
   */
  private var handlers:Seq[ContentItemBsonHandler] = Seq(ContentSequenceHandler, MarkdownPageHandler)
  
  /**
   * Register a new database handler for a content item.
   */
  def registerHandler(bh:ContentItemBsonHandler) = this.synchronized {
    handlers = handlers :+ bh
  }
  
  def update(i: Option[ContentItem]) = {
    (for {
      item <- i
      handler <- handlers.find(_.update.isDefinedAt(item))
    } yield handler.update(item)) getOrElse BSONDocument()
  }
  
  def create(i: Option[ContentItem]) = {
    for {
      item <- i
      handler <- handlers.find(_.create.isDefinedAt(item))
    } yield handler.create(item)
  }
  
  def read(doc:BSONDocument, kind:String) = {
    val h = handlers.find(_.read.isDefinedAt(kind, doc))
    h.map(_.read(kind, doc))
  }
}

trait ContentItemBsonHandler {
  
  def create:PartialFunction[ContentItem, BSONDocument]
  
  def update:PartialFunction[ContentItem, BSONDocument] 
  
  def read:PartialFunction[(String, BSONDocument), ContentItem]
}

object MarkdownPageHandler extends ContentItemBsonHandler {
  
  val format = Macros.handler[MarkdownPage]
  
  def create = { case p:MarkdownPage => format.write(p) }
  
  def update = { case p:MarkdownPage => BSONDocument("item" -> format.write(p)) }
  
  def read = { case (MarkdownPage.itemType, doc) => format.read(doc) }
  
}
