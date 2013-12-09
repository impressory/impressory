package com.impressory.reactivemongo

import com.wbillingsley.handy.Ref
import Ref._

import reactivemongo.api._
import reactivemongo.bson._

import com.wbillingsley.handy.reactivemongo.DAO

import com.impressory.api._

object ContentEntryDAO extends DAO[ContentEntry] {
  
  val collName = "contentEntry"
    
  val db = DBConnector
  
  val clazz = classOf[ContentEntry]
  
  def unsaved = ContentEntry(id=allocateId)
    
  implicit val CETagsFormat = Macros.handler[CETags]
  
  implicit val CESettingsFormat = Macros.handler[CESettings]
  implicit val cic = ContentItemConverter
  
  /* Note that when we write a content entry we do not write the votes or comments */
  implicit object bsonWriter extends BSONDocumentWriter[ContentEntry] {
    def write(ce: ContentEntry) = BSONDocument(
      "course" -> ce.course, "addedBy" -> ce.addedBy,
      "kind" -> ce.kind,
      "tags" -> ce.tags,
      "title" -> ce.title, "note" -> ce.note, 
      "settings" -> ce.settings,
      "published" -> ce.published, "updated" -> ce.updated, "created" -> ce.created
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[ContentEntry] {
    def read(doc: BSONDocument): ContentEntry = {
      
      val item:Option[ContentItem] = for {
        kind <- doc.getAs[String]("kind")
        d <- doc.getAs[BSONDocument]("item")
        i <- ContentItemConverter.read(d, kind) 
      } yield i

      implicit val udvr = UpDownVotingReader
      implicit val ecr = EmbeddedCommentReader
      
      val entry = new ContentEntry(
        id = doc.getAs[BSONObjectID]("_id").get.stringify,
        course = doc.getRef(classOf[Course], "course"),
        addedBy = doc.getRef(classOf[User], "addedBy"),
        item = item,
        tags = doc.getAs[CETags]("tags").getOrElse(CETags()),
        title = doc.getAs[String]("title"),
        note = doc.getAs[String]("note"),
        settings = doc.getAs[CESettings]("settings").getOrElse(CESettings()),
        voting = doc.getAs[UpDownVoting]("voting").getOrElse(new UpDownVoting),
        commentCount = doc.getAs[Int]("commentCount").getOrElse(0),
        comments = doc.getAs[Seq[EmbeddedComment]]("comments").getOrElse(Seq.empty),
        published = doc.getAs[Long]("published"),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
      )
      entry
    }
  }  
  
  def byTopic(course:Ref[Course], topic:String) = {
    val query = BSONDocument("course" -> course, "tags.topics" -> topic, "settings.inIndex" -> true, "published" -> BSONDocument("$exists" -> true))
    findMany(query)
  }
  
  def byKind(course:Ref[Course], kind:String) = {
    val query = BSONDocument("course" -> course, "kind" -> kind, "published" -> BSONDocument("$exists" -> true))
    findMany(query)
  }  
  
  def inIndexByCourse(course:Ref[Course]) = {
    val query = BSONDocument("course" -> course, "settings.inIndex" -> true, "published" -> BSONDocument("$exists" -> true))
    findMany(query)
  }

  /**
   * Recently published entries that are listed as being in the news feed
   */
  def recentInNewsByCourse(course:Ref[Course]) = {
    val query = BSONDocument("course" -> course, "settings.inNews" -> true, "published" -> BSONDocument("$exists" -> true))
    val sort = BSONDocument("published" -> 1)
    findSorted(query, sort)
  }
  
  /**
   * Updates the Item in a ContentEntry
   */
  def setItem(ce:Ref[ContentEntry], item:ContentItem):Ref[ContentEntry] = {
    val query = BSONDocument("_id" -> ce)
    val update = BSONDocument("$set" -> BSONDocument("item" -> item, "updated" -> System.currentTimeMillis()))
    updateAndFetch(query, update)
  }
  
  def sequencesContaining(ce:Ref[ContentEntry]) = {
    findMany(BSONDocument("kind" -> ContentSequence.itemType, "item.entries" -> ce))
  }
  
  /**
   * Saves the content entry including its item. This tends to be used where editing the item also updates
   * the metadata (eg, changing the URL of a web page may change the site metadata)
   */
  def saveWithItem(ce:ContentEntry) = {
    val toSave = ce.copy(updated = System.currentTimeMillis())
    val doc = bsonWriter.write(toSave)
    val docWithItem = doc ++ BSONDocument("item" -> toSave.item)
    
    updateSafe(BSONDocument(idIs(ce.id)), BSONDocument("$set" -> docWithItem), toSave)
  }
  
  /**
   * Save a new item
   */
  def saveNew(ce:ContentEntry) = {
    val doc = bsonWriter.write(ce)
    val docWithItem = doc ++ BSONDocument(idIs(ce.id), "item" -> ce.item)
    saveSafe(doc, ce)
  }
  
  /**
   * Saves a content entry. Note this doesn't write the item, comments, or other componentns that are independently altered
   */
  def saveExisting(ce:ContentEntry) = {
    updateSafe(BSONDocument(idIs(ce.id)), BSONDocument("$set" -> ce), ce)
  }
  
  /**
   * Votes up
   */
  def voteUp(ce:ContentEntry, who:Ref[User]) = {
    if (!ce.voting.hasVoted(who)) {
      val query = BSONDocument(idIs(ce.id))
      val update = BSONDocument(
          "$addToSet" -> BSONDocument("voting._up" -> who),
          "$inc" -> BSONDocument("voting.score" -> 1)
      )
      updateAndFetch(query, update)
    } else {
      ce.itself
    }
  }

  /**
   * Votes down
   */
  def voteDown(ce:ContentEntry, who:Ref[User]) = {
    if (!ce.voting.hasVoted(who)) {
      val query = BSONDocument(idIs(ce.id))
      val update = BSONDocument(
          "$addToSet" -> BSONDocument("voting._down" -> who),
          "$inc" -> BSONDocument("voting.score" -> -1)
      )
      updateAndFetch(query, update)
    } else {
      ce.itself
    }
  }
  
  def addComment(ce:ContentEntry, who:Ref[User], text:String) = {
    val query = BSONDocument(idIs(ce.id))
    val update = BSONDocument(
        "$push" -> BSONDocument("comments" -> EmbeddedCommentWriter.writeNew(new EmbeddedComment(id=allocateId, text=text, addedBy=who))),
        "$inc" -> BSONDocument("commentCount" -> 1)
    )
    updateAndFetch(query, update)
  }
  
}