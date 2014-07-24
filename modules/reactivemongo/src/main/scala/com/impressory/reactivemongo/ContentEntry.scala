package com.impressory.reactivemongo

import reactivemongo.api._
import reactivemongo.bson._

import com.wbillingsley.handy.{Id, Ref, RefWithId, RefFuture}
import Ref._
import Id._

import com.wbillingsley.handy.reactivemongo.DAO

import com.impressory.api._

object ContentEntryDAO extends DAO with com.impressory.api.dao.ContentEntryDAO {
  
  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  type DataT = ContentEntry
  
  val collName = "contentEntry"
    
  val db = DBConnector
  
  val clazz = classOf[ContentEntry]

  val executionContext = RefFuture.executionContext

  import CommonFormats._
    
  implicit val CETagsFormat = Macros.handler[CETags]
  
  implicit val CESettingsFormat = Macros.handler[CESettings]

  implicit val CEMessageFormat = Macros.handler[CEMessage]

  /* Note that when we write a content entry we do not write the votes or comments */
  implicit object bsonWriter extends BSONDocumentWriter[ContentEntry] {
    def write(ce: ContentEntry) = BSONDocument(
      "course" -> ce.course, "addedBy" -> ce.addedBy,
      "kind" -> ce.kind,
      "tags" -> ce.tags,
      "message" -> ce.message,
      "settings" -> ce.settings,
      "updated" -> ce.updated, "created" -> ce.created
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[ContentEntry] {
    def read(doc: BSONDocument): ContentEntry = {
      
      val item:Option[ContentItem] = for {
        kind <- doc.getAs[String]("kind")
        d <- doc.getAs[BSONDocument]("item")
        i <- ContentItemToBson.read(d, kind) 
      } yield i

      val entry = new ContentEntry(
        id = doc.getAs[Id[ContentEntry, String]]("_id").get,
        course = doc.getAs[Id[Course, String]]("course").get,
        addedBy = doc.getAs[Id[User, String]]("addedBy").get,
        item = item,
        tags = doc.getAs[CETags]("tags").getOrElse(CETags()),
        message = doc.getAs[CEMessage]("message").getOrElse(new CEMessage),
        settings = doc.getAs[CESettings]("settings").getOrElse(CESettings()),
        voting = doc.getAs[UpDownVoting]("voting").getOrElse(new UpDownVoting),
        comments = doc.getAs[Comments]("comments").getOrElse(new Comments),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
      )
      entry
    }
  }  
  
  def byTopic(course:RefWithId[Course], topic:String) = {
    val query = BSONDocument("course" -> course.getId, "tags.topics" -> topic, "settings.inIndex" -> true, "settings.published" -> BSONDocument("$exists" -> true))
    findMany(query)
  }
  
  def byKind(course:RefWithId[Course], kind:String) = {
    val query = BSONDocument("course" -> course.getId, "kind" -> kind, "settings.published" -> BSONDocument("$exists" -> true))
    findMany(query)
  }
  
  def myDrafts(user:Ref[User], course:Ref[Course]) = {
    val query = for {
      courseId <- course.refId
      userId <- user.refId
    } yield BSONDocument(
      "settings.published" -> BSONDocument("$exists" -> false), "addedBy" -> userId, "course" -> courseId
    )
    query flatMap findMany
  }
  
  def inIndexByCourse(course:RefWithId[Course]) = {
    val query = BSONDocument("course" -> course.getId, "settings.inIndex" -> true, "settings.published" -> BSONDocument("$exists" -> true))
    findMany(query)
  }

  /**
   * Recently published entries that are listed as being in the news feed
   */
  def recentInNewsByCourse(course:RefWithId[Course]) = {
    val query = BSONDocument("course" -> course.getId, "settings.inNews" -> true, "settings.published" -> BSONDocument("$exists" -> true))
    val sort = BSONDocument("settings.published" -> 1)
    findSorted(query, sort)
  }
  
  /**
   * Updates the Item in a ContentEntry
   */
  def setItem(ce:RefWithId[ContentEntry], item:ContentItem):Ref[ContentEntry] = {
    val query = BSONDocument("_id" -> ce.getId)
    val update = BSONDocument("$set" -> (BSONDocument("updated" -> System.currentTimeMillis()) ++ ContentItemToBson.update(Some(item))))
    updateAndFetch(query, update)
  }
  
  def sequencesContaining(ce:RefWithId[ContentEntry]) = {
    findMany(BSONDocument("kind" -> ContentSequence.itemType, "item.entries" -> ce.getId))
  }
  
  /**
   * Saves the content entry including its item. This tends to be used where editing the item also updates
   * the metadata (eg, changing the URL of a web page may change the site metadata)
   */
  def saveWithItem(ce:ContentEntry) = {
    val toSave = ce.copy(updated = System.currentTimeMillis())
    val doc = bsonWriter.write(toSave)
    val docWithItem = doc ++ ContentItemToBson.update(toSave.item)
    
    updateSafe(BSONDocument(idIs(ce.id)), BSONDocument("$set" -> docWithItem), toSave)
  }
  
  /**
   * Save a new item
   */
  def saveNew(ce:ContentEntry) = {
    val doc = bsonWriter.write(ce)
    val docWithItem = doc ++ BSONDocument(idIs(ce.id), "item" -> ContentItemToBson.create(ce.item))
    saveSafe(docWithItem, ce)
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
  def voteUp(ce:ContentEntry, who:RefWithId[User]) = {
    if (!ce.voting.hasVoted(who)) {
      val query = BSONDocument(idIs(ce.id))
      val update = BSONDocument(
          "$addToSet" -> BSONDocument("voting.up" -> who.getId),
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
  def voteDown(ce:ContentEntry, who:RefWithId[User]) = {
    if (!ce.voting.hasVoted(who)) {
      val query = BSONDocument(idIs(ce.id))
      val update = BSONDocument(
          "$addToSet" -> BSONDocument("voting.down" -> who.getId),
          "$inc" -> BSONDocument("voting.score" -> -1)
      )
      updateAndFetch(query, update)
    } else {
      ce.itself
    }
  }
  
  def addComment(ce:ContentEntry, who:Id[User,String], text:String) = {
    val query = BSONDocument(idIs(ce.id))
    val update = BSONDocument(
        "$push" -> BSONDocument("comments.embedded" -> new EmbeddedComment(id=allocateId.asId[EmbeddedComment], text=text, addedBy=who)),
        "$inc" -> BSONDocument("comments.count" -> 1)
    )
    updateAndFetch(query, update)
  }
  
}