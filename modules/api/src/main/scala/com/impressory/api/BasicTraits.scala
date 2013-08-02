package com.impressory.api

/**
 * Marks classes that can be serialised to send them to the client
 */
trait CanSendToClient {
  // No required methods
}

trait HasStringId {
  def id:Option[String]
}