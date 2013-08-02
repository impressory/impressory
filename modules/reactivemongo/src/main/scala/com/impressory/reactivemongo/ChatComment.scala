package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Enumeratee
import com.wbillingsley.handyplay.RefEnumIter
import com.impressory.api.{UserError, CanSendToClient}

trait RecordedChatEvent extends CanSendToClient {
  /**
   * When we store more than one kind of chat event, this will be needed to determine how to deserialise an event
   */
  val eventType: String
}

case class ChatComment (
    
  val text:String,

  val anonymous:Boolean = true,

  val course:Ref[Course] = None,

  val addedBy:Ref[User] = None,

  val session:Option[String] = None,

  val topics:Set[String] = Set.empty[String],
    
  val created: Long = System.currentTimeMillis,

  val _id: BSONObjectID = BSONObjectID.generate
    
) extends HasBSONId with RecordedChatEvent {
  
  /**
   * Two entries are equal if they have the same ID
   */
  override def equals(obj: Any) = {
    obj.isInstanceOf[ChatComment] &&
      obj.asInstanceOf[ChatComment].id == id
  }

  def id = _id
    
  val eventType = ChatComment.eventType
  
}

object ChatComment extends FindById[ChatComment] {
  
  val eventType = "chatComment"
  
  val collName = "chatComment"
    
  implicit val clazz = classOf[ChatComment]
    
  /* Note that when we write a content entry we do not write the votes or comments */
  implicit object bsonWriter extends BSONDocumentWriter[ChatComment] {
    def write(cc: ChatComment) = BSONDocument(
      "anon" -> cc.anonymous, "course" -> cc.course, "addedBy" -> cc.addedBy,
      "session" -> cc.session, "text" -> cc.text, "topics" -> cc.topics, 
      "created" -> cc.created, "_id" -> cc._id
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[ChatComment] {
    def read(doc: BSONDocument): ChatComment = {
      
      val cc = new ChatComment(
        anonymous = doc.getAs[Boolean]("anon").getOrElse(false),
        course = doc.getRef(classOf[Course], "course"),
        addedBy = doc.getRef(classOf[User], "addedBy"),
        session = doc.getAs[String]("session"),
        text = doc.getAs[String]("text").getOrElse("(no text)"),
        topics = doc.getAs[Set[String]]("topics").getOrElse(Set.empty),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        _id = doc.getAs[BSONObjectID]("_id").get
      )
      cc
    }
  }  
  
  def byTopic(course:Ref[Course], topic:String):RefMany[ChatComment] = {
    val res = for (cid <- course.getId) yield {
      val query = BSONDocument("course" -> cid, "topics" -> topic)
      val coll = DB.coll(collName)
      val cursor = coll.find(query).cursor[ChatComment]
      new RefEnumIter(cursor.enumerateBulks)
    }
    res.getOrElse(RefNone)
    
  }
  
  /**
   * Save a new item
   */
  def saveNew(cc:ChatComment) = {
    val doc = bsonWriter.write(cc)
    val fle = DB.coll(collName).save(doc)
    val fut = fle.map { _ => cc.itself } recover { case x:Throwable => RefFailed(x) }
    new RefFutureRef(fut)    
  }
  
  
  def lastFew(course:Ref[Course]) = {
      val query = BSONDocument("course" -> course)
      val sort = BSONDocument("_id" -> -1)
      val coll = DB.coll(collName)
      val cursor = coll.find(query).sort(sort).cursor[ChatComment]
      new RefEnumIter(cursor.enumerateBulks &> Enumeratee.take(1))    
  }
}