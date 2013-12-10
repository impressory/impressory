package com.impressory.plugins

import com.impressory.api._


object ContentItemViews {

  /**
   * The list of handlers for different kinds of content item. 
   */
  private var handlers:Seq[ContentItemViewHandler] = Seq.empty
  
  /**
   * Register a new database handler for a content item.
   */
  def registerHandler(bh:ContentItemViewHandler) = this.synchronized {
    handlers = handlers :+ bh
  }
  
  def main(kind:String) = {
    handlers.find(_.main.isDefinedAt(kind)).map(_.main(kind))
  }
  
  def stream(kind:String) = {
    handlers.find(_.stream.isDefinedAt(kind)).map(_.stream(kind))
  }

  def edit(kind:String) = {
    handlers.find(_.edit.isDefinedAt(kind)).map(_.edit(kind))
  }
}

trait ContentItemViewHandler {
  
  def main:PartialFunction[String, String]
  
  def stream:PartialFunction[String, String] 
  
  def edit:PartialFunction[String, String]
}