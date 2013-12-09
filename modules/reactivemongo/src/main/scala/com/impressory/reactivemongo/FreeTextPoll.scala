package com.impressory.reactivemongo

import reactivemongo.bson._
import reactivemongo.api._

import com.impressory.api._
import com.impressory.api.poll._

object FreeTextPollReader extends BSONDocumentReader[FreeTextPoll] {
  def read(doc: BSONDocument) = new FreeTextPoll(
    text = doc.getAs[String]("text"),
    resultsVis = PollResultsVisibility.valueOf(doc.getAs[String]("resultsVis").getOrElse("secret"))
  )
}
  
object FreeTextPollWriter extends BSONDocumentWriter[FreeTextPoll] {    
  def write(p: FreeTextPoll) = BSONDocument(
    "text" -> p.text, "resultsVis" -> p.resultsVis.toString
  )
}