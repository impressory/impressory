package com.impressory.reactivemongo

import com.wbillingsley.handy.RefManyById
import reactivemongo.api._
import reactivemongo.bson._

import com.impressory.api._

object UpDownVotingReader extends BSONDocumentReader[UpDownVoting] {
  def read(doc:BSONDocument):UpDownVoting = {
    new UpDownVoting(
      up = new RefManyById(classOf[User], doc.getAs[Set[BSONObjectID]]("_up").getOrElse(Set.empty).toSeq.map(_.stringify)),
      down = new RefManyById(classOf[User], doc.getAs[Set[BSONObjectID]]("_down").getOrElse(Set.empty).toSeq.map(_.stringify)),
      score = doc.getAs[Int]("score").getOrElse(0)
    )
  }
}