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

import CommonFormats._


object ChatCommentDAO extends DAO {
  
  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  type DataT = ChatComment
  
  val collName = "chatComment"
    
  val db = DBConnector

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
    
  implicit val clazz = classOf[ChatComment]
    
  /* Note that when we write a content entry we do not write the votes or comments */
  implicit object bsonWriter extends BSONDocumentWriter[ChatComment] {
    def write(cc: ChatComment) = BSONDocument(
      "anon" -> cc.anonymous, "course" -> writeId(cc.course), "addedBy" -> writeId(cc.addedBy),
      "session" -> cc.session, "text" -> cc.text, "topics" -> cc.topics, 
      "created" -> cc.created, idIs(cc.id)
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[ChatComment] {
    def read(doc: BSONDocument): ChatComment = {
      
      val cc = new ChatComment(
        anonymous = doc.getAs[Boolean]("anon").getOrElse(false),
        course = doc.getAs[Id[Course, String]]("course").get,
        addedBy = doc.getAs[Id[User, String]]("addedBy"),
        session = doc.getAs[String]("session"),
        text = doc.getAs[String]("text").getOrElse("(no text)"),
        topics = doc.getAs[Set[String]]("topics").getOrElse(Set.empty),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        id = doc.getAs[Id[ChatComment, String]]("_id").get
      )
      cc
    }
  }  
  
  def byTopic(course:RefWithId[Course], topic:String):RefMany[ChatComment] = {
    findMany(BSONDocument("course" -> course.getId, "topics" -> topic))
  }
  
  /**
   * Save a new item
   */
  def saveNew(cc:ChatComment) = saveSafe(bsonWriter.write(cc), cc)
  
  
  def lastFew(course:RefWithId[Course]) = {
    findSorted(BSONDocument("course" -> course.getId), BSONDocument("_id" -> -1))
  }
}