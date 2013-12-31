package com.impressory.plugins

import com.impressory.api._

/**
 * Registry for rendering templates for events in the chat room
 */
object EventViews {

  type handler = PartialFunction[String, String]
  
  /**
   * The list of handlers for different kinds of content item. 
   */
  private var handlers:Seq[handler] = Seq.empty
  
  /**
   * Register a new database handler for a content item.
   */
  def registerHandler(h:handler) = this.synchronized {
    handlers = handlers :+ h
  }
  
  def view(kind:String) = {
    println("EventView is looking for " + kind)
    handlers.find(_.isDefinedAt(kind)).map(_(kind))
  }
  
}
