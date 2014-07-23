package com.impressory.poll.multipleChoice

import com.impressory.plugins.LookUps
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handyplay._
import com.wbillingsley.handyplay.RefConversions._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.security._

// Import the DataAction configuration
import com.impressory.plugins.RouteConfig.dataActionConfig

// Import the currently configured LookUps
import com.impressory.plugins.LookUps._

object MCPollController extends Controller {
  
  implicit val usertoj = com.impressory.reactivemongo.UserDAO
  implicit val mcprtoj = MCPollResponseToJson
  
  /**
   * Called when a vote arrives by POST
   */
  def vote(pid:String) = DataAction.returning.one(parse.json) { implicit request =>
    val p = for {
        e <- LazyId.of[ContentEntry](pid);
        u <- request.approval.who
        answer <- Ref((request.body \ "options").asOpt[Set[Int]]) orIfNone UserError("No options in that vote");
        previous <- optionally(MCPollResponseDAO.byUserOrSession(e.itself, u.itself, Some(request.sessionKey)));
        pr <- previous match {
          case Some(v) => {
            val now = System.currentTimeMillis()
            val updated = v.copy(
              answer = answer,
              session = Some(request.sessionKey),
              responses = v.responses :+ PastResponse(answer, Some(request.sessionKey), updated=now),
              updated = now
            )
            MCPollResponseDAO.updateVote(updated)
          }
          case None => {
            val now = System.currentTimeMillis()
            val updated = MCPollResponse(
              id = LookUps.allocateId,
              poll = e.id,
              addedBy = Some(u.id),
              answer = answer,
              session = Some(request.sessionKey),
              responses = Seq(PastResponse(answer, Some(request.sessionKey), updated=now)),
              updated = now
            )
            MCPollResponseDAO.newVote(updated)
          }
        } 
    } yield {
      val adjustments = scala.collection.mutable.Map[Int,Int]()
      
      for (idx <- pr.answer) adjustments(idx) = 1
      for (prev <- previous; idx <- prev.answer) adjustments(idx) = adjustments.getOrElse(idx, 0) - 1
      
      com.impressory.eventroom.EventRoom.default ! Vote(e.id, adjustments.toMap)
      
      pr
    }
    // TODO: We shouldn't need to do this
    p
  }
  
  def getVote(pid:String) = DataAction.returning.one { implicit request =>
    val poll = LazyId.of[ContentEntry](pid)
    MCPollResponseDAO.byUserOrSession(poll, request.user, Some(request.sessionKey))
  }
  
  /**
   * Pushes the poll to the interaction stream
   */
  def pushMCPollToStream(pid:String) = DataAction.returning.result { implicit request =>
    
    val resp = for (
        poll <- LazyId.of[ContentEntry](pid);
        approved <- request.approval ask Permissions.chat(poll.course)
    ) yield {
      com.impressory.eventroom.EventRoom.default ! PushPollToChat(poll)
      Ok("")
    }
    resp    
    
    
  }

}