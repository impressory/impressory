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
    
  val _ce:Option[BSONObjectID] = None,

  val _course:Option[BSONObjectID] = None,

  var _entries:Seq[BSONObjectID] = Seq.empty,
  
  val updated: Long = System.currentTimeMillis,

  val created: Long = System.currentTimeMillis,

  val _id: BSONObjectID = BSONObjectID.generate
  
) extends HasBSONId with ContentItem {

  val itemType = ContentSequence.itemType
  
  def id = _id

  def course = Ref.fromOptionId(classOf[Course], _course)

  def ce = Ref.fromOptionId(classOf[ContentEntry], _ce)
   
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
    def write(s: ContentSequence) = BSONDocument(
      "_id" -> s._id, "course" -> s._course, "ce" -> s._ce,
      "entries" -> s._entries,
      "created" -> s.created, "updated" -> s.updated
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[ContentSequence] {
    def read(doc: BSONDocument): ContentSequence = {
      new ContentSequence(
        _id = doc.getAs[BSONObjectID]("_id").get,
        _course = doc.getAs[BSONObjectID]("course"),
        _ce = doc.getAs[BSONObjectID]("ce"),
        _entries = doc.getAs[Seq[BSONObjectID]]("entries").getOrElse(Seq.empty),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)        
      )
    }
  }
  
  def unsaved(course:Ref[Course], ce:Ref[ContentEntry]) = {
    new ContentSequence(_course = course.getId, _ce = ce.getId)
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

