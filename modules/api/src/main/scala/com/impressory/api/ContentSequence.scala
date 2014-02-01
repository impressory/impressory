package com.impressory.api

import com.wbillingsley.handy._

case class ContentSequence( 
    
  entries:RefManyById[ContentEntry, String] = RefManyById.empty

)(implicit lookupCE:LookUp[ContentEntry, String]) extends ContentItem {
  
  val itemType = ContentSequence.itemType

  /**
   * Whether this Content Sequence includes the given ContentEntry
   */
  def contains(entry:RefWithId[ContentEntry]) = {
    entry.getId match {
      case Some(id) => entries.rawIds.contains(id)
      case _ => false
    }
  }
  
  def indexOf(entry:RefWithId[ContentEntry]) = {
    entry.getId match {
      case Some(id) => entries.rawIds.indexOf(id)
      case _ => -1
    }    
  }  
  
  def start = entries.first
  
}

object ContentSequence {
  val itemType = "sequence"
}

case class EntryInSequence(entry: ContentEntry, index:Option[Int])