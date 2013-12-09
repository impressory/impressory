package com.impressory.api.poll

import com.impressory.api._

case class FreeTextPoll(
  var text:Option[String],
  
  var resultsVis: PollResultsVisibility = PollResultsVisibility.secret
) extends ContentItem {
  val itemType = FreeTextPoll.itemType
}

object FreeTextPoll {
  
  val itemType = "Free text poll"
  
}