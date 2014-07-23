package com.impressory.api

import com.wbillingsley.handy._
import Ref._
import Id.AsId

case class ContentSequence( 
    
  entries:Ids[ContentEntry, String] = new Ids(Seq.empty)

)(implicit lookupCE:LookUp[ContentEntry, String]) extends ContentItem {
  
  val itemType = ContentSequence.itemType

  /**
   * Whether this Content Sequence includes the given ContentEntry
   */
  def contains(entry:RefWithId[ContentEntry]) = {
    entry.getId match {
      case Some(id) => entries.ids.contains(id)
      case _ => false
    }
  }
  
  def indexOf(entry:RefWithId[ContentEntry]) = {
    entry.getId match {
      case Some(id) => entries.ids.indexOf(id)
      case _ => -1
    }    
  }  
  
  def start = {
    val id = for {
      s <- entries.ids.headOption
    } yield s.asId[ContentEntry]
    id.toRef.flatMap(_.lazily)
  }

}

object ContentSequence {
  val itemType = "sequence"
}

case class EntryInSequence(entry: ContentEntry, index:Option[Int])