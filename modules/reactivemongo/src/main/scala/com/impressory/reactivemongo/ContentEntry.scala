package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handyplay.RefEnumIter
import com.impressory.api.UserError


class ContentEntry (
    
  val course: Ref[Course],
  
  val addedBy: Ref[User],
  
  var kind: Option[String] = None,
  
  var item: Option[ContentItem] = None,
  
  var adjectives: Set[String] = Set.empty,
  
  var nouns: Set[String] = Set.empty,
  
  var topics: Set[String] = Set.empty,
  
  var site:String = "local",
  
  var title:Option[String] = None,
  
  var note:Option[String] = None,
  
  var showFirst: Boolean = false,
  
  var protect: Boolean = false,
  
  var inTrash: Boolean = false,
  
  val voting: UpDownVoting = new UpDownVoting,
  
  var commentCount:Int = 0,

  var comments:Seq[EmbeddedComment] = Seq.empty,
    
  var updated: Long = System.currentTimeMillis,

  val created: Long = System.currentTimeMillis,

  val _id: BSONObjectID = BSONObjectID.generate
    
) extends HasBSONId {
  
  /**
   * Two entries are equal if they have the same ID
   */
  override def equals(obj: Any) = {
    obj.isInstanceOf[ContentEntry] &&
      obj.asInstanceOf[ContentEntry].id == id
  }

  def id = _id
  
}

object ContentEntry extends FindById[ContentEntry] {
  
  val collName = "contentEntry"
    
  /* Note that when we write a content entry we do not write the votes or comments */
  implicit object bsonWriter extends BSONDocumentWriter[ContentEntry] {
    def write(ce: ContentEntry) = BSONDocument(
      "course" -> ce.course, "addedBy" -> ce.addedBy,
      "kind" -> ce.kind,
      "adjs" -> ce.adjectives, "nouns" -> ce.nouns, "topics" -> ce.topics,
      "site" -> ce.site, "title" -> ce.title, "note" -> ce.note, 
      "showFirst" -> ce.showFirst, "protect" -> ce.protect, "inTrash" -> ce.inTrash,
      "updated" -> ce.updated, "created" -> ce.created
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[ContentEntry] {
    def read(doc: BSONDocument): ContentEntry = {
      
      val kind = doc.getAs[String]("kind").getOrElse("")
      
      val item:Option[ContentItem] = kind match {
        case ContentSequence.itemType => doc.getAs[ContentSequence]("item")
        case WebPage.itemType => doc.getAs[WebPage]("item")
        case GoogleSlides.itemType => doc.getAs[GoogleSlides]("item")
        case YouTubeVideo.itemType => doc.getAs[YouTubeVideo]("item")
        case MarkdownPage.itemType => doc.getAs[MarkdownPage]("item")
        case MultipleChoicePoll.itemType => doc.getAs[MultipleChoicePoll]("item")
        case _ => None
      }

      val entry = new ContentEntry(
        _id = doc.getAs[BSONObjectID]("_id").get,
        course = doc.getRef(classOf[Course], "course"),
        addedBy = doc.getRef(classOf[User], "addedBy"),
        kind = doc.getAs[String]("kind"),
        item = item,
        adjectives = doc.getAs[Set[String]]("adjs").getOrElse(Set.empty),
        nouns = doc.getAs[Set[String]]("nouns").getOrElse(Set.empty),
        topics = doc.getAs[Set[String]]("topics").getOrElse(Set.empty),
        site = doc.getAs[String]("site").getOrElse("local"),
        title = doc.getAs[String]("title"),
        note = doc.getAs[String]("note"),
        protect = doc.getAs[Boolean]("protect").getOrElse(false),
        voting = doc.getAs[UpDownVoting]("voting").getOrElse(new UpDownVoting),
        commentCount = doc.getAs[Int]("commentCount").getOrElse(0),
        comments = doc.getAs[Seq[EmbeddedComment]]("comments").getOrElse(Seq.empty),
        inTrash = doc.getAs[Boolean]("inTrash").getOrElse(false),
        showFirst = doc.getAs[Boolean]("showFirst").getOrElse(false),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
      )
      entry
    }
  }  
  
  def byTopic(course:Ref[Course], topic:String):RefMany[ContentEntry] = {
    println("topic is " + topic)
    
    val query = BSONDocument("course" -> course, "topics" -> topic)
    val coll = DB.coll(collName)
    val cursor = coll.find(query).cursor[ContentEntry]
    val rei = new RefEnumIter(cursor.enumerateBulks)
    rei
  }
  
  def byCourse(course:Ref[Course]):RefMany[ContentEntry] = {
    val res = for (cid <- course.getId) yield {
      val query = BSONDocument("course" -> cid)
      val coll = DB.coll(collName)
      val cursor = coll.find(query).cursor[ContentEntry]
      new RefEnumIter(cursor.enumerateBulks)
    }
    res.getOrElse(RefNone)
  }

  def unsaved(course: Ref[Course], addedBy: Ref[User], kind:Option[String] = None) = {
    new ContentEntry(course=course, addedBy=addedBy, kind=kind).itself
  }
  
  /**
   * Updates the Item in a ContentEntry
   */
  def setItem(ce:Ref[ContentEntry], item:ContentItem):Ref[ContentEntry] = {
    val query = BSONDocument("_id" -> ce)
    val update = BSONDocument("$set" -> BSONDocument("item" -> item, "updated" -> System.currentTimeMillis()))

    updateAndFetch(query, update)
  }
  
  /**
   * Saves the content entry including its item. This tends to be used where editing the item also updates
   * the metadata (eg, changing the URL of a web page may change the site metadata)
   */
  def saveWithItem(ce:ContentEntry) = {
    ce.updated = System.currentTimeMillis()
    val doc = bsonWriter.write(ce)
    val docWithItem = doc ++ BSONDocument("item" -> ce.item)
    
    updateSafe(BSONDocument("_id" -> ce.id), BSONDocument("$set" -> docWithItem), ce)
  }
  
  /**
   * Save a new item
   */
  def saveNew(ce:ContentEntry) = {
    val doc = bsonWriter.write(ce)
    val docWithItem = doc ++ BSONDocument("_id" -> ce._id, "item" -> ce.item)
    
    val fle = DB.coll(collName).save(docWithItem)
    val fut = fle.map { _ => ce.itself } recover { case x:Throwable => RefFailed(x) }
    new RefFutureRef(fut)    
  }
  
  /**
   * Saves a content entry. Note this doesn't write the item, comments, or other componentns that are independently altered
   */
  def saveExisting(ce:ContentEntry) = {
    updateSafe(BSONDocument("_id" -> ce.id), BSONDocument("$set" -> ce), ce)
  }
  
  /**
   * Votes up
   */
  def voteUp(ce:ContentEntry, who:Ref[User]) = {
    if (!ce.voting.hasVoted(who)) {
      val query = BSONDocument("_id" -> ce._id)
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
      val query = BSONDocument("_id" -> ce._id)
      val update = BSONDocument(
          "$addToSet" -> BSONDocument("voting._down" -> who),
          "$inc" -> BSONDocument("voting.score" -> -1)
      )
      updateAndFetch(query, update)
    } else {
      ce.itself
    }
  }  
  
}