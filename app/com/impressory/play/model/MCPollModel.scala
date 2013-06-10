package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import Permissions._
import play.api.libs.json._
import com.impressory.reactivemongo.PollResultsVisibility

object MCPollModel {
  
  var defaultText = "Now you need to edit this poll..."

  import MultipleChoicePoll._
  
  implicit val MCOptToJson = Json.format[MCOpt]
  
  implicit object ResultsVisToJson extends Format[PollResultsVisibility] {
    
    override def writes(p:PollResultsVisibility) = Json.toJson(p.toString)
    
    override def reads(j:JsValue) = JsSuccess(PollResultsVisibility.valueOf(j.as[String]))
    
  }
  
  implicit val MCPollToJson = Json.format[MultipleChoicePoll]
  
  implicit class MCPollResponseToJson(val resp:MCPollResponse) extends AnyVal {
    def toJson = Json.obj("answer" -> resp.answer)
  }
  
  
  def updateMCPoll(ce:ContentEntry, data:JsValue) = {
    ce.item match {
      case Some(p:MultipleChoicePoll) => {
        ce.item = Some((data \ "item").asOpt[MultipleChoicePoll].getOrElse(p))
      } 
      case _ => { /* ignore */ }
    }
    ce.itself
  }
  
  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def create(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    ce.site = "local"
    RefItself(new MultipleChoicePoll(Some(defaultText)))
  } 
  
  def vote(poll:Ref[ContentEntry], tok:Approval[User], session:Option[String], answer:Set[Int]) = {
    val response = new MCPollResponse(poll=poll, addedBy=tok.who, session=session, answer=answer)
    MCPollResponse.vote(poll, tok.who, session, response)
  }
}