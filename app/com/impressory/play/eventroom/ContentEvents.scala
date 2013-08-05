package com.impressory.play.eventroom

import com.impressory.api.CanSendToClient
import com.impressory.play.model.ContentEntry

object ContentEvents {

  case class ContentPublished(ce: ContentEntry) extends CanSendToClient

  case class ContentEdited(ce: ContentEntry) extends CanSendToClient
  
}