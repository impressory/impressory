package com.impressory.reactivemongo

import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handy._
import Ref._
import reactivemongo.bson._
import reactivemongo.api._
import com.wbillingsley.handyplay._
import com.impressory.api.CanSendToClient

case class MCPollResponse (
  
  _id: BSONObjectID = BSONObjectID.generate,
  
  poll: Ref[ContentEntry] = RefNone,
  
  addedBy: Ref[User] = RefNone,
  
  session: Option[String] = None,
  
  var answer:Set[Int] = Set.empty,
  
  var updated: Long = System.currentTimeMillis
) extends HasBSONId with CanSendToClient {
  def id = _id
}

object MCPollResponse extends FindById[MCPollResponse] {
  
  val collName = "mcPollResponse"
  
  implicit object bsonReader extends BSONDocumentReader[MCPollResponse] {
    def read(doc:BSONDocument):MCPollResponse = {
      new MCPollResponse(
        _id = doc.getAs[BSONObjectID]("_id").get,
        addedBy = doc.getAs[Ref[User]]("addedBy").getOrElse(RefNone),
        session = doc.getAs[String]("session"),
        answer = doc.getAs[Set[Int]]("answer").getOrElse(Set.empty),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis)
      )
    }
  }
  
  implicit object bsonWriter extends BSONDocumentWriter[MCPollResponse] {
    def write(r:MCPollResponse) = {
      BSONDocument(
        "poll" -> r.poll, "answer" -> r.answer, "updated" -> System.currentTimeMillis,
        "addedBy" -> r.addedBy, "session" -> r.session
      )
    }
  }
  
  /**
   * Finds a poll response by user (if one is provided) or by session key
   */
  def byUserOrSession(poll:Ref[ContentEntry], u:Ref[User], session:Option[String]) = {
    val query = u.getId match {
      case Some(uid) => BSONDocument("poll" -> poll, "addedBy" -> uid)
      case None => BSONDocument("poll" -> poll, "session" -> session)
    }
    new RefFutureOption(coll.find(query).one[MCPollResponse])
  }
  
  def byPoll(poll:Ref[ContentEntry]) ={
    new RefEnumerator(coll.find(BSONDocument("poll" -> poll)).cursor[MCPollResponse].enumerate)
  }
  
  def vote(poll:Ref[ContentEntry], u:Ref[User], session:Option[String], vote:MCPollResponse) = {
    val query = u.getId match {
      case Some(uid) => BSONDocument("poll" -> poll, "addedBy" -> uid)
      case None => BSONDocument("poll" -> poll, "session" -> session)
    }
    val fle = coll.update(query, vote, upsert=true)
    val fut = fle.map { _ => vote.itself } recover { case x:Throwable => RefFailed(x) }
    new RefFutureRef(fut)
  }
}