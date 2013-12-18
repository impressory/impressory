package com.impressory.poll.multipleChoice

import com.impressory.poll._
import reactivemongo.bson._
import com.impressory.reactivemongo.ContentItemBsonHandler

/**
 * BSON handler for database communication
 */
object BsonHandler extends ContentItemBsonHandler {
  
  import reactivemongo.bson._
  
  implicit val mcoFormat = Macros.handler[MCOpt]
  
  def create = { case p:MultipleChoicePoll => 
    BSONDocument(
      "text" -> p.text, "pick" -> p.pick, "resultsVis" -> p.resultsVis.toString, "feedbackVis" -> p.feedbackVis.toString, "options" -> p.options
    )
  }
  
  def update = { case p:MultipleChoicePoll => BSONDocument("item" -> create(p)) }

  def read = { case (MultipleChoicePoll.itemType, doc) =>
    new MultipleChoicePoll(
      text = doc.getAs[String]("text"),
      pick = doc.getAs[Int]("pick").getOrElse(1),
      resultsVis = PollResultsVisibility.valueOf(doc.getAs[String]("resultsVis").getOrElse("secret")),
      feedbackVis = PollResultsVisibility.valueOf(doc.getAs[String]("feedbackVis").getOrElse("secret")),
      options = doc.getAs[Seq[MCOpt]]("options").getOrElse(Seq.empty)
    )
  }    
}