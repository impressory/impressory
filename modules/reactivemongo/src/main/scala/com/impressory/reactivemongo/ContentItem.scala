package com.impressory.reactivemongo

import reactivemongo.bson._
import reactivemongo.api._
import com.impressory.api._

object ContentItemToBson {
  
  /**
   * The list of handlers for different kinds of content item. 
   */
  private var handlers:Seq[ContentItemBsonHandler] = Seq.empty
  
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

