package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Enumeratee
import play.api.libs.iteratee.Iteratee
import play.api.http.HeaderNames
import com.wbillingsley.eventroom.Subscribe
import com.impressory.api._
import com.impressory.security._
import com.impressory.play.model._
import com.impressory.play.eventroom._
import com.wbillingsley.handy.appbase.DataAction
import com.impressory.reactivemongo.MCPollResponseDAO


object PollController extends Controller {
  
  implicit val mcprtoj = com.impressory.play.model.MCPollResponseToJson
  
  /**
   * Called when a vote arrives by POST
   */
  def vote(courseId:String, pid:String) = DataAction.returning.one(parse.json) { implicit request =>

    import MCPollModel._
    
    for (
        e <- refContentEntry(pid);
        answer <- Ref((request.body \ "options").asOpt[Set[Int]]) orIfNone UserError("No options in that vote");
        previous <- optionally(MCPollResponseDAO.byUserOrSession(e.itself, request.approval.who, Some(request.sessionKey)));
        pr <- MCPollModel.vote(e.itself, request.approval, Some(request.sessionKey), answer)
    ) yield {
      val adjustments = scala.collection.mutable.Map[Int,Int]()
      
      for (idx <- pr.answer) adjustments(idx) = 1
      for (prev <- previous; idx <- prev.answer) adjustments(idx) = adjustments.getOrElse(idx, 0) - 1
      
      EventRoom.default ! MCPollEvents.Vote(e.id, adjustments.toMap)
      
      pr
    }
  }
  
  /**
   * Pushes the poll to the interaction stream
   */
  def pushMCPollToStream(courseId:String, pid:String) = DataAction.returning.result { implicit request =>
    
    import MCPollModel._
    
    val resp = for (
        poll <- refContentEntry(pid);
        approved <- request.approval ask Permissions.Chat(poll.course)
    ) yield {
      EventRoom.default ! MCPollEvents.PushPollToChat(poll)
      Ok("")
    }
    resp    
    
    
  }

}