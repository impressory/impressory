package com.impressory.reactivemongo

import com.wbillingsley.handy.RefManyById
import reactivemongo.api._
import reactivemongo.bson._

import com.impressory.api._

object UpDownVotingReader extends BSONDocumentReader[UpDownVoting] {

  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  def read(doc:BSONDocument):UpDownVoting = {
    new UpDownVoting(
      up = doc.getRefMany[User]("_up"),
      down = doc.getRefMany[User]("_down"),
      score = doc.getAs[Int]("score").getOrElse(0)
    )
  }
}