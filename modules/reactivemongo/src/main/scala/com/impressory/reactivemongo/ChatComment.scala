package com.impressory.reactivemongo

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.iteratee.Enumeratee

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay.RefEnumIter
import com.wbillingsley.handy.reactivemongo.DAO

import com.impressory.api._
import com.impressory.api.events._


object ChatCommentDAO extends DAO {
  
  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  type DataT = ChatComment
  
  val collName = "chatComment"
    
  val db = DBConnector

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
    
  implicit val clazz = classOf[ChatComment]
  
  def unsaved = ChatComment(id=allocateId, text="")
    
  /* Note that when we write a content entry we do not write the votes or comments */
  implicit object bsonWriter extends BSONDocumentWriter[ChatComment] {
    def write(cc: ChatComment) = BSONDocument(
      "anon" -> cc.anonymous, "course" -> cc.course, "addedBy" -> cc.addedBy,
      "session" -> cc.session, "text" -> cc.text, "topics" -> cc.topics, 
      "created" -> cc.created, idIs(cc.id)
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[ChatComment] {
    def read(doc: BSONDocument): ChatComment = {
      
      val cc = new ChatComment(
        anonymous = doc.getAs[Boolean]("anon").getOrElse(false),
        course = doc.getRef[Course]("course"),
        addedBy = doc.getRef[User]("addedBy"),
        session = doc.getAs[String]("session"),
        text = doc.getAs[String]("text").getOrElse("(no text)"),
        topics = doc.getAs[Set[String]]("topics").getOrElse(Set.empty),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        id = doc.getAs[BSONObjectID]("_id").get.stringify
      )
      cc
    }
  }  
  
  def byTopic(course:RefWithId[Course], topic:String):RefMany[ChatComment] = {
    findMany(BSONDocument("course" -> course, "topics" -> topic))
  }
  
  /**
   * Save a new item
   */
  def saveNew(cc:ChatComment) = saveSafe(bsonWriter.write(cc), cc)
  
  
  def lastFew(course:RefWithId[Course]) = {
    findSorted(BSONDocument("course" -> course), BSONDocument("_id" -> -1))
  }
}