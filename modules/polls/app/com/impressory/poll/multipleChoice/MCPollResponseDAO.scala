package com.impressory.poll.multipleChoice

import com.wbillingsley.handy.{Ref, RefNone}
import com.wbillingsley.handy.reactivemongo.DAO
import reactivemongo.bson._
import reactivemongo.api._

import com.impressory.api._
import com.impressory.poll._
import com.impressory.reactivemongo._

object MCPollResponseDAO extends DAO {
  
  type DataT = MCPollResponse
  
  val collName = "mcPollResponse"
  
  val db = DBConnector
  
  val clazz = classOf[MCPollResponse]
  
  def unsaved = MCPollResponse(id=allocateId)

  implicit val pastRespHandler = Macros.handler[PastResponse]
  implicit object bsonReader extends BSONDocumentReader[MCPollResponse] {
    def read(doc:BSONDocument):MCPollResponse = {
      new MCPollResponse(
        id = doc.getAs[BSONObjectID]("_id").get.stringify,
        poll = doc.getAs[Ref[ContentEntry]]("poll").getOrElse(RefNone),
        addedBy = doc.getAs[Ref[User]]("addedBy").getOrElse(RefNone),
        session = doc.getAs[String]("session"),
        answer = doc.getAs[Set[Int]]("answer").getOrElse(Set.empty),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
        responses = doc.getAs[Seq[PastResponse]]("responses").getOrElse(Seq.empty)
      )
    }
  }
  
  implicit object bsonWriter extends BSONDocumentWriter[MCPollResponse] {
    def write(r:MCPollResponse) = {
      BSONDocument(
        idIs(r.id),
        "poll" -> r.poll, "answer" -> r.answer, "updated" -> System.currentTimeMillis,
        "addedBy" -> r.addedBy, "session" -> r.session, "responses" -> r.responses
      )
    }
  }
  
  /**
   * Finds a poll response by user (if one is provided) or by session key
   */
  def byUserOrSession(poll:Ref[ContentEntry], u:Ref[User], session:Option[String]) = {
    val query = u.getId match {
      case Some(uid) => BSONDocument("poll" -> poll, "addedBy" -> u)
      case None => BSONDocument("poll" -> poll, "session" -> session)
    }
    findOne(query)
  }
  
  def byPoll(poll:Ref[ContentEntry]) ={
    findMany(BSONDocument("poll" -> poll))
  }
  
  def updateVote(vote:MCPollResponse) = {
    saveSafe(bsonWriter.write(vote), vote)
  }
  
  
  
  def newVote(vote:MCPollResponse) = {
    saveSafe(bsonWriter.write(vote), vote)
  }
}