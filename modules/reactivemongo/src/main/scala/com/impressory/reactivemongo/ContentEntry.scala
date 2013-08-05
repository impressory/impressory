package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handyplay.RefEnumIter
import com.impressory.api.{UserError, CanSendToClient}


case class CESettings(
  var showFirst: Boolean = false,
  
  var protect: Boolean = false,
  
  var inTrash: Boolean = false,
  
  var inNews: Boolean = true,
  
  var inIndex: Boolean = true
)

case class CETags(
  var adjectives: Set[String] = Set.empty,
  
  var nouns: Set[String] = Set.empty,
  
  var topics: Set[String] = Set.empty,
  
  var site: Option[String] = None    
)

class ContentEntry (
    
  val course: Ref[Course] = RefNone,
  
  val addedBy: Ref[User] = RefNone,
  
  var item: Option[ContentItem] = None,
  
  var tags: CETags = CETags(),
  
  var title:Option[String] = None,
  
  var note:Option[String] = None,
  
  var settings: CESettings = CESettings(),
  
  val voting: UpDownVoting = new UpDownVoting,
  
  val commentCount:Int = 0,

  val comments:Seq[EmbeddedComment] = Seq.empty,
    
  var updated: Long = System.currentTimeMillis,
  
  var published: Option[Long] = None,

  val created: Long = System.currentTimeMillis,

  val _id: BSONObjectID = BSONObjectID.generate
    
) extends HasBSONId with CanSendToClient {
  
  def kind = item.map(_.itemType)
  
  def setPublished(p:Boolean) {
    if (p) {
      published = published orElse Some(System.currentTimeMillis())
    } else {
      published = None
    }
  }
  
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
    
  implicit val CETagsFormat = Macros.handler[CETags]
  
  implicit val CESettingsFormat = Macros.handler[CESettings]
  
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
  
  def byTopic(course:Ref[Course], topic:String):RefMany[ContentEntry] = {
    println("topic is " + topic)
    
    val query = BSONDocument("course" -> course, "tags.topics" -> topic, "settings.inIndex" -> true, "published" -> BSONDocument("$exists" -> true))
    val coll = DB.coll(collName)
    val cursor = coll.find(query).cursor[ContentEntry]
    val rei = new RefEnumIter(cursor.enumerateBulks)
    rei
  }
  
  def inIndexByCourse(course:Ref[Course]):RefMany[ContentEntry] = {
    val query = BSONDocument("course" -> course, "settings.inIndex" -> true, "published" -> BSONDocument("$exists" -> true))
    coll.find(query).cursor[ContentEntry].refMany
  }

  /**
   * Recently published entries that are listed as being in the news feed
   */
  def recentInNewsByCourse(course:Ref[Course]):RefMany[ContentEntry] = {
    val query = BSONDocument("course" -> course, "settings.inNews" -> true, "published" -> BSONDocument("$exists" -> true))
    val sort = BSONDocument("published" -> 1)
    coll.find(query).sort(sort).cursor[ContentEntry].refMany
  }

  
  def unsaved(course: Ref[Course], addedBy: Ref[User]) = {
    new ContentEntry(course=course, addedBy=addedBy).itself
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
  
  def addComment(ce:ContentEntry, who:Ref[User], text:String) = {
    val query = BSONDocument("_id" -> ce._id)
    val update = BSONDocument(
        "$push" -> BSONDocument("comments" -> EmbeddedComment.bsonWriter.writeNew(new EmbeddedComment(text=text, addedBy=who))),
        "$inc" -> BSONDocument("commentCount" -> 1)
    )
    updateAndFetch(query, update)
  }
  
}