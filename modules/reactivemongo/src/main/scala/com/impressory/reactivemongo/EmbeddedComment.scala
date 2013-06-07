package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handyplay.RefEnumIter
import com.impressory.api.UserError


/**
 * A comment that is embedded in another item
 */
case class EmbeddedComment (
  var text:String = "",

  addedBy:Ref[User] = RefNone,
  
  created:Long = System.currentTimeMillis,
  
  voting:UpDownVoting = new UpDownVoting,
  
  val _id:BSONObjectID = BSONObjectID.generate
) extends HasBSONId {
  def id = _id
}

object EmbeddedComment {
  
  implicit object bsonReader extends BSONDocumentReader[EmbeddedComment] {
    def read(doc:BSONDocument):EmbeddedComment = {
      new EmbeddedComment(
        _id = doc.getAs[BSONObjectID]("_id").get,
        text = doc.getAs[String]("text").get,
        addedBy = doc.getAs[Ref[User]]("addedBy").getOrElse(RefNone),
        voting = doc.getAs[UpDownVoting]("voting").getOrElse(new UpDownVoting)
      )
    }
  }
  
  /* Note that when we write an answer we do not write the votes, comments, or answers */
  implicit object bsonWriter extends BSONDocumentWriter[EmbeddedComment] {
    def write(c: EmbeddedComment) = BSONDocument(
      "text" -> c.text
    )

    // For writing new answers
    def writeNew(c: EmbeddedComment) = write(c) ++ BSONDocument(
      "addedBy" -> c.addedBy, "created" -> c.created, "_id" -> c._id
    )
  
  }  
  
}