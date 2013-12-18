package com.impressory.poll.freeText

import com.impressory.poll._
import reactivemongo.bson._
import com.impressory.reactivemongo.ContentItemBsonHandler

/**
 * BSON handler for database communication
 */
object BsonHandler extends ContentItemBsonHandler {
  
  import reactivemongo.bson._
  
  def create = { case p:FreeTextPoll => 
    BSONDocument(
      "text" -> p.text, "resultsVis" -> p.resultsVis.toString
    )
  }
  
  def update = { case p:FreeTextPoll => BSONDocument("item" -> create(p)) }

  def read = { case (FreeTextPoll.itemType, doc) =>
    new FreeTextPoll(
      text = doc.getAs[String]("text"),
      resultsVis = PollResultsVisibility.valueOf(doc.getAs[String]("resultsVis").getOrElse("secret"))
    )
  }    
}