package com.impressory.api.poll

import com.impressory.api._

object MultipleChoicePoll {
  
  val itemType = "Multiple choice poll"
    
  implicit def strToOpt(s:String) = MCOpt(s)
  
  case class MCOpt(option:String, feedback:Option[String]=None, score:Int=0)
  
}

case class MultipleChoicePoll(
  var text: Option[String] = None, 
  var pick:Int = 1,
  var resultsVis: PollResultsVisibility = PollResultsVisibility.secret,
  var feedbackVis: PollResultsVisibility = PollResultsVisibility.secret,
  var options: Seq[MultipleChoicePoll.MCOpt] = Seq("A", "B", "C", "E")
) extends ContentItem {
  
  val itemType = MultipleChoicePoll.itemType
  
}

