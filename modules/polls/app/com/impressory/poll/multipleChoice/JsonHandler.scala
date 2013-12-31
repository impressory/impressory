package com.impressory.poll.multipleChoice

import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.poll._
import com.impressory.json._
import com.impressory.plugins._
import com.impressory.security.Permissions._
import com.wbillingsley.handy.appbase.JsonConverter
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json.toJsFieldJsValueWrapper

object JsonHandler extends ContentItemJsonHandler {
  
  var defaultText = "Now you need to edit this poll..."

  import MultipleChoicePoll._
  
  implicit val MCOptToJson = Json.format[MCOpt]
  
  implicit object ResultsVisToJson extends Format[PollResultsVisibility] {
    
    override def writes(p:PollResultsVisibility) = Json.toJson(p.toString)
    
    override def reads(j:JsValue) = JsSuccess(PollResultsVisibility.valueOf(j.as[String]))
    
  }
  
  implicit val MCPollToJson = Json.format[MultipleChoicePoll]
  
  def urlChecker(blank:ContentEntry, url:String) = RefNone
  
  def toJsonFor = { case (entry, p: MultipleChoicePoll, appr) =>
    MCPollToJson.writes(p).itself
  }
  
  def updateFromJson = { case (MultipleChoicePoll.itemType, json, before) =>
    before.item match {
      case Some(p:MultipleChoicePoll) => {
        before.copy(item = Some((json \ "item").asOpt[MultipleChoicePoll].getOrElse(p))).itself
      } 
      case _ => RefFailed(new IllegalStateException("Attempted to update something that wasn't a multiple choice poll as if it was"))
    }
  }
  
  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def createFromJson= { case (MultipleChoicePoll.itemType, json, blank) =>
    blank.copy(
      tags = blank.tags.copy(site=None),
      item = Some(new MultipleChoicePoll(Some(defaultText)))
    ).itself
  } 
  
}

object MCPollResponseToJson extends JsonConverter[MCPollResponse, User] {
  
  def toJson(resp:MCPollResponse) = Json.obj("answer" -> resp.answer).itself
  
  def toJsonFor(resp:MCPollResponse, appr:Approval[User]) = toJson(resp)
}