package com.impressory.reactivemongo

import com.wbillingsley.handy.{Ref, RefNone}

import reactivemongo.api._
import reactivemongo.bson._

import com.impressory.api._

// Import the configuration to create RefByIds (where to look them up)
import com.impressory.plugins.LookUps._

  
object EmbeddedCommentReader extends BSONDocumentReader[EmbeddedComment] {

  implicit val udvReader = UpDownVotingReader
  
  
  def read(doc:BSONDocument):EmbeddedComment = {
    new EmbeddedComment(
      id = doc.getAs[BSONObjectID]("_id").get.stringify,
      text = doc.getAs[String]("text").get,
      addedBy = doc.getRef[User]("addedBy"),
      voting = doc.getAs[UpDownVoting]("voting").getOrElse(new UpDownVoting),
      created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
    )
  }
}
  
/* Note that when we write an answer we do not write the votes, comments, or answers */
object EmbeddedCommentWriter extends BSONDocumentWriter[EmbeddedComment] {
  
  import UserDAO.RefWithStringIdWriter
  import UserDAO.idIs
  
  def write(c: EmbeddedComment) = BSONDocument(
    "text" -> c.text
  )

  // For writing new answers
  def writeNew(c: EmbeddedComment) = write(c) ++ BSONDocument(
    "addedBy" -> c.addedBy, "created" -> c.created, idIs(c.id)
  )
}  
