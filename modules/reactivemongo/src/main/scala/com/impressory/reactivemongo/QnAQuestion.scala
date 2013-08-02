package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handyplay.RefEnumIter
import com.impressory.api.{UserError, CanSendToClient}

/**
 * A votable question in a Q&A forum
 */
case class QnAQuestion(

  var title:String,

  var text:String,

  val course:Ref[Course] = RefNone,
  
  val addedBy:Ref[User] = RefNone,

  val session:Option[String] = None,
    
  val voting:UpDownVoting = new UpDownVoting,
  
  var views:Int = 0,
  
  var tags:Set[String] = Set.empty,
  
  var commentCount:Int = 0,

  var comments:Seq[EmbeddedComment] = Seq.empty,
    
  var answerCount:Int = 0,
  
  var answers:Seq[QnAAnswer] = Seq.empty,
  
  var answerAccepted:Boolean = false,

  val created:Long = System.currentTimeMillis,
  
  var updated:Long = System.currentTimeMillis,
  
  val _id:BSONObjectID = BSONObjectID.generate  

) extends HasBSONId with CanSendToClient {
  
  def id = _id
   
}


object QnAQuestion extends FindById[QnAQuestion] {  
  
  val collName = "qnaQuestion"
  
  /* Note that when we write a content entry we do not write the votes, comments, or answers */
  implicit object bsonWriter extends BSONDocumentWriter[QnAQuestion] {
    def write(q: QnAQuestion) = BSONDocument(
      "course" -> q.course, "addedBy" -> q.addedBy, "session" -> q.session, 
      "title" -> q.title, "text" -> q.text,
      "tags" -> q.tags,
      "updated" -> System.currentTimeMillis
    )
    
    def writeNew(q:QnAQuestion) = write(q) ++ BSONDocument(
      "created" -> q.created, "_id" -> q._id
    )
  }
  
  val ibsonReader = reactivemongo.bson.Macros.reader[QnAQuestion]
  
  implicit object bsonReader extends BSONDocumentReader[QnAQuestion] {
    def readi(doc:BSONDocument):QnAQuestion = ibsonReader.read(doc) 
    
    def read(doc:BSONDocument):QnAQuestion = {
      new QnAQuestion(
        _id = doc.getAs[BSONObjectID]("_id").get,
        title = doc.getAs[String]("title").getOrElse("(no title)"),
        text = doc.getAs[String]("text").getOrElse("(no title)"),
        course = doc.getAs[Ref[Course]]("course").getOrElse(RefNone),
        addedBy = doc.getAs[Ref[User]]("addedBy").getOrElse(RefNone),
        session = doc.getAs[String]("session"), 
        voting = doc.getAs[UpDownVoting]("UpdownVoting").getOrElse(new UpDownVoting),
        views = doc.getAs[Int]("views").getOrElse(0),
        tags = doc.getAs[Set[String]]("tags").getOrElse(Set.empty),
        commentCount = doc.getAs[Int]("commentCount").getOrElse(0),
        comments = doc.getAs[Seq[EmbeddedComment]]("comments").getOrElse(Seq.empty),
        answerCount = doc.getAs[Int]("answerCount").getOrElse(0),
        answers = doc.getAs[Seq[QnAAnswer]]("answers").getOrElse(Seq.empty),
        answerAccepted = doc.getAs[Boolean]("answerAccepted").getOrElse(false),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis), 
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis) 
      )
    }
  }
  
  /**
   * Increments the number of views for this question.
   */
  def incrementViewed(q:Ref[QnAQuestion]) {
    val update = BSONDocument("$inc" -> BSONDocument("views" -> 1))
    val query = BSONDocument("_id" -> q)
    val fle = DB.coll(collName).update(query, update)
  }
  
  /**
   * Adds an answer to the question, returning an updated question
   */
  def addAnswer(q:Ref[QnAQuestion], ans:QnAAnswer) = {
    val query = BSONDocument("_id" -> q)
    val update = BSONDocument(
        "$inc" -> BSONDocument("answerCount" -> 1),
        "$push" -> BSONDocument("answers" -> QnAAnswer.bsonWriter.writeNew(ans))
    )
    val fle = DB.coll(collName).update(query, update)
    val fo = fle.flatMap { le => coll.find(query).one[QnAQuestion] }
    new RefFutureOption(fo)
  }

  def addQComment(q:Ref[QnAQuestion], ec:EmbeddedComment) = {
    val query = BSONDocument("_id" -> q)
    val update = BSONDocument(
        "$inc" -> BSONDocument("commentCount" -> 1),
        "$push" -> BSONDocument("comments" -> EmbeddedComment.bsonWriter.writeNew(ec))
    )
    val fle = coll.update(query, update)
    val fo = fle.flatMap { le => coll.find(query).one[QnAQuestion] }
    new RefFutureOption(fo)
  }  

  def addAnsComment(q:Ref[QnAQuestion], a:Ref[QnAAnswer], ec:EmbeddedComment) = {
    val query = BSONDocument("_id" -> q, "answers._id" -> a)
    val update = BSONDocument(
        "$inc" -> BSONDocument("answers.$.commentCount" -> 1),
        "$push" -> BSONDocument("answers.$.comments" -> EmbeddedComment.bsonWriter.writeNew(ec))
    )
    val fle = coll.update(query, update)
    val fo = fle.flatMap { le => coll.find(query).one[QnAQuestion] }
    new RefFutureOption(fo)
  }  
  
  
  def byCourse(course:Ref[Course], skip:Option[Int] = None, limit:Int = 50) = {
    val query = BSONDocument("course" -> course)
    val cursor = coll.find(query).cursor[QnAQuestion]
    val rei = new RefEnumIter(cursor.enumerateBulks)
    rei
  }
  
  def saveNew(q:QnAQuestion) = {
    val query = BSONDocument("_id" -> q._id)
    val update = bsonWriter.writeNew(q)
    val fle = coll.save(update)
    val fo = fle.flatMap { le => coll.find(query).one[QnAQuestion] }
    new RefFutureOption(fo)
  }

}
