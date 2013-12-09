package com.impressory.reactivemongo

import reactivemongo.bson._
import reactivemongo.api._

import com.impressory.api.poll._
import MultipleChoicePoll._



object MultipleChoicePollReader extends BSONDocumentReader[MultipleChoicePoll] {
  
  implicit val format = reactivemongo.bson.Macros.reader[MCOpt]
  
  def read(doc: BSONDocument) = new MultipleChoicePoll(
    text = doc.getAs[String]("text"),
    pick = doc.getAs[Int]("pick").getOrElse(1),
    resultsVis = PollResultsVisibility.valueOf(doc.getAs[String]("resultsVis").getOrElse("secret")),
    feedbackVis = PollResultsVisibility.valueOf(doc.getAs[String]("feedbackVis").getOrElse("secret")),
    options = doc.getAs[Seq[MCOpt]]("options").getOrElse(Seq.empty)
  )
}
  
object MultipleChoicePollWriter extends BSONDocumentWriter[MultipleChoicePoll] {

  implicit val format = reactivemongo.bson.Macros.writer[MCOpt]

  def write(p: MultipleChoicePoll) = BSONDocument(
    "text" -> p.text, "pick" -> p.pick, "resultsVis" -> p.resultsVis.toString, "feedbackVis" -> p.feedbackVis.toString, "options" -> p.options
  )
} 
  
