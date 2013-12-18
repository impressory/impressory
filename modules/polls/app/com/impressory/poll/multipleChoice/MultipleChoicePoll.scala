package com.impressory.poll.multipleChoice

import com.impressory.api._
import com.impressory.poll.PollResultsVisibility

case class MCOpt(option:String, feedback:Option[String]=None, score:Int=0)

object MultipleChoicePoll {
  
  val itemType = "Multiple choice poll"
    
  implicit def strToOpt(s:String) = MCOpt(s)
  
}

case class MultipleChoicePoll(
  var text: Option[String] = None, 
  var pick:Int = 1,
  var resultsVis: PollResultsVisibility = PollResultsVisibility.secret,
  var feedbackVis: PollResultsVisibility = PollResultsVisibility.secret,
  var options: Seq[MCOpt] = Seq("A", "B", "C", "E").map(MCOpt(_))
) extends ContentItem {
  
  val itemType = MultipleChoicePoll.itemType
  
}

