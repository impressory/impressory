package com.impressory.poll.multipleChoice

import com.wbillingsley.handy.{Id, Ref, RefNone, RefWithId}
import Ref._
import com.wbillingsley.handy.reactivemongo.DAO
import reactivemongo.bson._
import reactivemongo.api._

import com.impressory.api._
import com.impressory.poll._
import com.impressory.reactivemongo._

import com.impressory.plugins.LookUps._

object MCPollResponseDAO extends DAO {
  
  type DataT = MCPollResponse
  
  val collName = "mcPollResponse"
  
  val db = DBConnector
  
  val clazz = classOf[MCPollResponse]

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  import CommonFormats._

  implicit val pastRespHandler = Macros.handler[PastResponse]
  implicit object bsonReader extends BSONDocumentReader[MCPollResponse] {
    def read(doc:BSONDocument):MCPollResponse = {
      new MCPollResponse(
        id = doc.getAs[Id[MCPollResponse, String]]("_id").get,
        poll = doc.getAs[Id[ContentEntry, String]]("poll").get,
        addedBy = doc.getAs[Id[User, String]]("addedBy"),
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
  def byUserOrSession(poll:RefWithId[ContentEntry], u:Ref[User], session:Option[String]) = {
    val rQuery = for {
      uid <- optionally(u.refId)
    } yield {
      uid match {
        case Some(id) => BSONDocument("poll" -> poll.getId, "addedBy" -> id)
        case None => BSONDocument("poll" -> poll.getId, "session" -> session)
      }
    }
    rQuery flatMap findOne
  }
  
  def byPoll(poll:RefWithId[ContentEntry]) ={
    findMany(BSONDocument("poll" -> poll.getId))
  }
  
  def updateVote(vote:MCPollResponse) = {
    saveSafe(bsonWriter.write(vote), vote)
  }
  
  
  
  def newVote(vote:MCPollResponse) = {
    saveSafe(bsonWriter.write(vote), vote)
  }
}