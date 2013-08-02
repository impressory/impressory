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
case class QnAAnswer(

  addedBy:Ref[User] = RefNone,

  val session:Option[String] = None,

  var text:String,
  
  voting:UpDownVoting = new UpDownVoting,
  
  var views:Int = 0,  
  
  var comments:Seq[EmbeddedComment] = Seq.empty,
    
  var accepted:Boolean = false,
  
  val created:Long = System.currentTimeMillis,
  
  var updated:Long = System.currentTimeMillis,
  
  _id:BSONObjectID = BSONObjectID.generate  

) extends HasBSONId with CanSendToClient {
  def id = _id
}


object QnAAnswer {
  
  implicit object bsonReader extends BSONDocumentReader[QnAAnswer] {
    def read(doc:BSONDocument):QnAAnswer = {
      new QnAAnswer(
        _id = doc.getAs[BSONObjectID]("_id").get,
        text = doc.getAs[String]("text").get,
        addedBy = doc.getAs[Ref[User]]("addedBy").getOrElse(RefNone),
        session  = doc.getAs[String]("session"),
        voting = doc.getAs[UpDownVoting]("voting").getOrElse(new UpDownVoting),
        views = doc.getAs[Int]("views").getOrElse(0),
        comments = doc.getAs[Seq[EmbeddedComment]]("comments").getOrElse(Seq.empty),
        accepted = doc.getAs[Boolean]("accepted").getOrElse(false),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis)
      )
    }
  }  
  
  /* Note that when we write an answer we do not write the votes, comments, or answers */
  implicit object bsonWriter extends BSONDocumentWriter[QnAAnswer] {
    def write(a: QnAAnswer) = BSONDocument(
      "text" -> a.text, "updated" -> System.currentTimeMillis()
    )

    // For writing new answers
    def writeNew(a: QnAAnswer) = write(a) ++ BSONDocument(
      "addedBy" -> a.addedBy, "session" -> a.session, "created" -> a.created, "_id" -> a._id
    )
  
  }
  
}