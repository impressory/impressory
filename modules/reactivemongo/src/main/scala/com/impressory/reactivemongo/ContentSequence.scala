package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handyplay.RefEnumIter

/**
 * From Presentation in the previous version
 */
class ContentSequence( 
    
  var _entries:Seq[BSONObjectID] = Seq.empty,
  
  val updated: Long = System.currentTimeMillis,

  val created: Long = System.currentTimeMillis
  
) extends ContentItem {

  val itemType = ContentSequence.itemType
  
  def entries = new RefManyById(classOf[ContentEntry], _entries)

  def entries_=(l:RefManyById[ContentEntry, _]) {
    _entries = l.getIds
  }
  
  /**
   * Whether this Content Sequence includes the given ContentEntry
   */
  def contains(entry:Ref[ContentEntry]) = {
    entry.getId match {
      case Some(id) => _entries.contains(id)
      case _ => false
    }
  }
  
  def indexOf(entry:Ref[ContentEntry]) = {
    entry.getId match {
      case Some(id) => _entries.indexOf(id)
      case _ => -1
    }    
  }
  
  def start = entries.first
  
}

object ContentSequence {
  
  val itemType = "sequence"
      
  implicit object bsonWriter extends BSONDocumentWriter[ContentSequence] {
    def write(s: ContentSequence) = {
      val doc = BSONDocument(
        "entries" -> s._entries,
        "created" -> s.created, "updated" -> s.updated
      )
      println("I have been asked to write the entries and they are " + s._entries)
      doc
    }
  }
  
  implicit object bsonReader extends BSONDocumentReader[ContentSequence] {
    def read(doc: BSONDocument): ContentSequence = {
      new ContentSequence(
        _entries = doc.getAs[Seq[BSONObjectID]]("entries").getOrElse(Seq.empty),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)        
      )
    }
  }
  
  def unsaved(course:Ref[Course], ce:Ref[ContentEntry]) = {
    new ContentSequence()
  }
  
  def containing(entry:Ref[ContentEntry]) = {
    val res = for (cid <- entry.getId) yield {
      val query = BSONDocument("kind" -> itemType, "item.entries" -> cid)
      val coll = DB.coll(ContentEntry.collName)
      val cursor = coll.find(query).cursor[ContentEntry]
      new RefEnumIter(cursor.enumerateBulks)
    }
    res.getOrElse(RefNone)
  }
}

