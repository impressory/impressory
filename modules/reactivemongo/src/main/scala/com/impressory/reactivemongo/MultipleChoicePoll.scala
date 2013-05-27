package com.impressory.reactivemongo

import reactivemongo.bson._
import reactivemongo.api._

case class MultipleChoicePoll(
  text: String, 
  pick:Int = 1,
  resultsVis: PollResultsVisibility = PollResultsVisibility.secret,
  feedbackVis: PollResultsVisibility = PollResultsVisibility.secret,
  options: Seq[MultipleChoicePoll.MCOpt] = Seq.empty
) extends ContentItem {
  
  val itemType = MultipleChoicePoll.itemType
  
}

object MultipleChoicePoll {
  
  val itemType = "Mutliple choice poll"
  
  case class MCOpt(option:String, feedback:String, score:Int)
  
  object MCOpt {
    implicit val format = reactivemongo.bson.Macros.handler[MCOpt]
  }
  
  implicit object bsonReader extends BSONDocumentReader[MultipleChoicePoll] {
    def read(doc: BSONDocument) = new MultipleChoicePoll(
      text = doc.getAs[String]("text").get,
      pick = doc.getAs[Int]("pick").getOrElse(1),
      resultsVis = PollResultsVisibility.valueOf(doc.getAs[String]("resultsVis").getOrElse("secret")),
      feedbackVis = PollResultsVisibility.valueOf(doc.getAs[String]("feedbackVis").getOrElse("secret")),
      options = doc.getAs[Seq[MCOpt]]("options").getOrElse(Seq.empty)
    )
  }
  
  implicit object bsonWriter extends BSONDocumentWriter[MultipleChoicePoll] {
    
    def write(p: MultipleChoicePoll) = BSONDocument(
      "text" -> p.text, "pick" -> p.pick, "resultsVis" -> p.resultsVis.toString, "feedbackVis" -> p.feedbackVis.toString, "options" -> p.options
    )
  } 
  
}