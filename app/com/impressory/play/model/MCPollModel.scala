package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.api.poll._
import com.impressory.plugins._
import com.impressory.security.Permissions
import Permissions._
import com.impressory.reactivemongo.MCPollResponseDAO
import com.wbillingsley.handy.appbase.JsonConverter

object MCPollModel extends ContentItemJsonHandler[MultipleChoicePoll] {
  
  var defaultText = "Now you need to edit this poll..."

  import MultipleChoicePoll._
  
  implicit val MCOptToJson = Json.format[MCOpt]
  
  implicit object ResultsVisToJson extends Format[PollResultsVisibility] {
    
    override def writes(p:PollResultsVisibility) = Json.toJson(p.toString)
    
    override def reads(j:JsValue) = JsSuccess(PollResultsVisibility.valueOf(j.as[String]))
    
  }
  
  implicit val MCPollToJson = Json.format[MultipleChoicePoll]
  
  val clazz = classOf[MultipleChoicePoll]
  
  val kind = MultipleChoicePoll.itemType
  
  def urlChecker(blank:ContentEntry, url:String) = RefNone
  
  def toJsonFor(p:MultipleChoicePoll, appr:Approval[User]) = {
    MCPollToJson.writes(p).itself
  }
  
  def updateFromJson(before:ContentEntry, json:JsValue) = {
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
  def createFromJson(blank:ContentEntry, json:JsValue) = {
    blank.copy(
      tags = blank.tags.copy(site=None),
      item = Some(new MultipleChoicePoll(Some(defaultText)))
    ).itself
  } 
  
  def vote(poll:Ref[ContentEntry], tok:Approval[User], session:Option[String], answer:Set[Int]) = {
    val response = MCPollResponseDAO.unsaved.copy(poll=poll, addedBy=tok.who, session=session, answer=answer)
    MCPollResponseDAO.vote(poll, tok.who, session, response)
  }
}

object MCPollResponseToJson extends JsonConverter[MCPollResponse, User] {
  
  def toJson(resp:MCPollResponse) = Json.obj("answer" -> resp.answer).itself
  
  def toJsonFor(resp:MCPollResponse, appr:Approval[User]) = toJson(resp)
}