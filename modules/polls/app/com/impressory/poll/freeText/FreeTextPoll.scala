package com.impressory.poll.freeText

import com.impressory.api._
import com.impressory.poll.PollResultsVisibility

case class FreeTextPoll(
  var text:Option[String],
  
  var resultsVis: PollResultsVisibility = PollResultsVisibility.secret
) extends ContentItem {
  val itemType = FreeTextPoll.itemType
}

object FreeTextPoll {
  
  val itemType = "Free text poll"
  
}