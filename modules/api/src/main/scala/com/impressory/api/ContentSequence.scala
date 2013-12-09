package com.impressory.api

import com.wbillingsley.handy.{RefManyById, Ref}

case class ContentSequence( 
    
  entries:RefManyById[ContentEntry, String] = new RefManyById(classOf[ContentEntry], Seq.empty)

) extends ContentItem {
  
  val itemType = ContentSequence.itemType

  /**
   * Whether this Content Sequence includes the given ContentEntry
   */
  def contains(entry:Ref[ContentEntry]) = {
    entry.getId match {
      case Some(id) => entries.rawIds.contains(id)
      case _ => false
    }
  }
  
  def indexOf(entry:Ref[ContentEntry]) = {
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