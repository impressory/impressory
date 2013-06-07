package com.impressory.reactivemongo

import reactivemongo.bson._
import reactivemongo.api._

import MultipleChoicePoll._

case class MultipleChoicePoll(
  var text: Option[String] = None, 
  var pick:Int = 1,
  var resultsVis: PollResultsVisibility = PollResultsVisibility.secret,
  var feedbackVis: PollResultsVisibility = PollResultsVisibility.secret,
  var options: Seq[MultipleChoicePoll.MCOpt] = Seq(MCOpt("A"), MCOpt("B"), MCOpt("C"), MCOpt("D"), MCOpt("E"))
) extends ContentItem {
  
  val itemType = MultipleChoicePoll.itemType
  
}

object MultipleChoicePoll {
  
  val itemType = "Multiple choice poll"
  
  case class MCOpt(option:String, feedback:Option[String]=None, score:Int=0)
  
  object MCOpt {
    implicit val format = reactivemongo.bson.Macros.handler[MCOpt]
  }
  
  implicit object bsonReader extends BSONDocumentReader[MultipleChoicePoll] {
    def read(doc: BSONDocument) = new MultipleChoicePoll(
      text = doc.getAs[String]("text"),
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