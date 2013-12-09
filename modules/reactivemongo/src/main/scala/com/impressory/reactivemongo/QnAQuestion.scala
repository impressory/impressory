package com.impressory.reactivemongo

import com.wbillingsley.handy.Ref

import reactivemongo.api._
import reactivemongo.bson._

import com.wbillingsley.handy.{Ref, RefNone}

import com.impressory.api._
import com.impressory.api.qna._

object QnAQuestionDAO  {  
    
  /* Note that when we write a content entry we do not write the votes, comments, or answers */
  implicit object bsonWriter extends BSONDocumentWriter[QnAQuestion] {
    def write(q: QnAQuestion) = BSONDocument(
      "text" -> q.text
    )
  }

  implicit val udvr = UpDownVotingReader
  implicit val ecr = EmbeddedCommentReader
  
  import ContentEntryDAO.RefWriter
  import ContentEntryDAO.idIs
  
  implicit object bsonReader extends BSONDocumentReader[QnAQuestion] {
    def readi(doc:BSONDocument):QnAQuestion = ibsonReader.read(doc) 
    
    def read(doc:BSONDocument):QnAQuestion = {
      new QnAQuestion(
        text = doc.getAs[String]("text").getOrElse("(no title)"),
        answerCount = doc.getAs[Int]("answerCount").getOrElse(0),
        answers = doc.getAs[Seq[QnAAnswer]]("answers").getOrElse(Seq.empty),
        answerAccepted = doc.getAs[Boolean]("answerAccepted").getOrElse(false)
      )
    }
  }
  
  implicit object QnAAnswerReader extends BSONDocumentReader[QnAAnswer] {
    def read(doc:BSONDocument):QnAAnswer = {
      new QnAAnswer(
        id = doc.getAs[BSONObjectID]("_id").get.stringify,
        text = doc.getAs[String]("text").get,
        addedBy = doc.getAs[Ref[User]]("addedBy").getOrElse(RefNone),
        session  = doc.getAs[String]("session"),
        voting = doc.getAs[UpDownVoting]("voting").getOrElse(new UpDownVoting),
        comments = doc.getAs[Seq[EmbeddedComment]]("comments").getOrElse(Seq.empty),
        accepted = doc.getAs[Boolean]("accepted").getOrElse(false),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis)
      )
    }
  }
  
  val ibsonReader = reactivemongo.bson.Macros.reader[QnAQuestion]
  
  
  /* Note that when we write an answer we do not write the votes, comments, or answers */
  implicit object QnAAnswerWriter extends BSONDocumentWriter[QnAAnswer] {
    def write(a: QnAAnswer) = BSONDocument(
      "text" -> a.text, "updated" -> System.currentTimeMillis()
    )
  
    def writeNew(a: QnAAnswer) = write(a) ++ BSONDocument(
      "addedBy" -> a.addedBy, "session" -> a.session, "created" -> a.created, idIs(a.id)
    ) 
  }
  
  /**
   * Adds an answer to the question, returning an updated question
   */
  def addAnswer(q:Ref[ContentEntry], ans:QnAAnswer) = {
    val query = BSONDocument("_id" -> q, "kind" -> QnAQuestion.itemType)
    val update = BSONDocument(
        "$inc" -> BSONDocument("answerCount" -> 1),
        "$push" -> BSONDocument("answers" -> QnAAnswerWriter.writeNew(ans))
    )
    ContentEntryDAO.updateAndFetch(query, update)
  }

  def addAnsComment(q:Ref[ContentEntry], a:Ref[QnAAnswer], ec:EmbeddedComment) = {
    val query = BSONDocument("_id" -> q, "answers._id" -> a)
    val update = BSONDocument(
        "$inc" -> BSONDocument("answers.$.commentCount" -> 1),
        "$push" -> BSONDocument("answers.$.comments" -> EmbeddedCommentWriter.writeNew(ec))
    )
    ContentEntryDAO.updateAndFetch(query, update)
  }  

}
