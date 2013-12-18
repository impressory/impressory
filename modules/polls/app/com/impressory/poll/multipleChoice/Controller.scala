package com.impressory.poll.multipleChoice

import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.security._
import com.wbillingsley.handy.appbase.DataAction



object MCPollController extends Controller {
  
  implicit val usertoj = com.impressory.reactivemongo.UserDAO
  implicit val mcprtoj = MCPollResponseToJson
  
  /**
   * Called when a vote arrives by POST
   */
  def vote(pid:String) = DataAction.returning.one(parse.json) { implicit request =>

    for (
        e <- new LazyId(classOf[ContentEntry], pid);
        answer <- Ref((request.body \ "options").asOpt[Set[Int]]) orIfNone UserError("No options in that vote");
        previous <- optionally(MCPollResponseDAO.byUserOrSession(e.itself, request.approval.who, Some(request.sessionKey)));
        pr <- JsonHandler.vote(e.itself, request.approval, Some(request.sessionKey), answer)
    ) yield {
      val adjustments = scala.collection.mutable.Map[Int,Int]()
      
      for (idx <- pr.answer) adjustments(idx) = 1
      for (prev <- previous; idx <- prev.answer) adjustments(idx) = adjustments.getOrElse(idx, 0) - 1
      
      com.impressory.eventroom.EventRoom.default ! Vote(e.id, adjustments.toMap)
      
      pr
    }
  }
  
  /**
   * Pushes the poll to the interaction stream
   */
  def pushMCPollToStream(pid:String) = DataAction.returning.result { implicit request =>
    
    val resp = for (
        poll <- new LazyId(classOf[ContentEntry], pid);
        approved <- request.approval ask Permissions.Chat(poll.course)
    ) yield {
      com.impressory.eventroom.EventRoom.default ! PushPollToChat(poll)
      Ok("")
    }
    resp    
    
    
  }

}