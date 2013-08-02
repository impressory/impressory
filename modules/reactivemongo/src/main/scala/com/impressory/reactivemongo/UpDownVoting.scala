package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handyplay.RefEnumIter
import com.impressory.api.{UserError, CanSendToClient}

case class UpDownVoting(
    
  _up:Set[BSONObjectID] = Set.empty,
  
  _down:Set[BSONObjectID] = Set.empty,
  
  score: Int = 0
    
) extends CanSendToClient {
  
  def up = new RefManyById(classOf[User], _up.toSeq)
  
  def down = new RefManyById(classOf[User], _down.toSeq)

  def hasVoted(u:Ref[User]) = {
    val id = u.getId.get
    _up.contains(id) || _down.contains(id)
  }
}

object UpDownVoting {
  
  implicit object bsonReader extends BSONDocumentReader[UpDownVoting] {
    def read(doc:BSONDocument):UpDownVoting = {
      new UpDownVoting(
        _up = doc.getAs[Set[BSONObjectID]]("_up").getOrElse(Set.empty),
        _down = doc.getAs[Set[BSONObjectID]]("_down").getOrElse(Set.empty),
        score = doc.getAs[Int]("score").getOrElse(0)
      )
    }
  }
  
}