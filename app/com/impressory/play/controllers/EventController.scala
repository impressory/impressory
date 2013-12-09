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
import com.impressory.play.model._
import com.impressory.play.eventroom._
import com.impressory.api._
import com.impressory.api.events._
import com.impressory.security.Permissions
import com.wbillingsley.handy.appbase.DataAction
import com.impressory.reactivemongo.ChatCommentDAO

/**
 * Controller handling subscriptions and connections to the EventRoom
 */
object EventController extends Controller {
  
  implicit val cctoj = com.impressory.json.ChatCommentToJson
  
  def subscribe = DataAction.returning.result(parse.json) { implicit request => 
    val approval = request.approval
    val oListenerName = (request.body \ "listenerName").asOpt[String]
    
    val resp = (request.body \ "subscription" \ "type").asOpt[String] match {
      case Some("course") => {
        val oCourseId = (request.body \ "subscription" \ "courseId").asOpt[String]
        for (
          courseId <- Ref(oCourseId);
          listenerName <- Ref(oListenerName);
          c <- RefById(classOf[Course], courseId);
          approved <- approval ask Permissions.Read(c.itself)
        ) yield {
          EventRoom.default ! Subscribe(listenerName, ChatStream(courseId))
          Ok(Json.obj("ok" -> "ok"))        
        }
      }
      case Some("Multiple choice poll results") => {
        val oceId = (request.body \ "subscription" \ "id").asOpt[String]
        for (
          ceId <- Ref(oceId);
          listenerName <- Ref(oListenerName);
          c <- RefById(classOf[ContentEntry], ceId);
          approved <- approval ask Permissions.ReadEntry(c.itself)
        ) yield {
          EventRoom.default ! Subscribe(listenerName, MCPollEvents.PollStream(ceId))
          Ok(Json.obj("ok" -> "ok"))        
        }
        
      }
      case _ => Ok(Json.obj("error" -> "Nothing to subscribe to")).itself 
    }
    resp
  }
  
  def serverSentEvents = DataAction.returning.result { implicit request =>
    
    for (
      u <- optionally(request.user)// orIfNone RefFailed(UserError("You must be logged in to listen for updates"))
    ) yield {
      val mem = Mem(u)
      val context = "Direct connection"
      EventRoom.serverSentEvents(listenerName=request.sessionKey, u=mem, session=request.sessionKey, context=context)
    }      
  }
  
  
  def postChatMessage(courseId:String) = DataAction.returning.result(parse.json) { implicit request => 
        
    for (
      c <- refCourse(courseId);
      approved <- request.approval ask Permissions.Chat(c.itself);
      text <- Ref((request.body \ "text").asOpt[String].map(_.trim)) if (!text.isEmpty);
      anon <- Ref((request.body \ "anonymous").asOpt[Boolean].orElse(Some(false)));
      cm = ChatCommentDAO.unsaved.copy(text=text, course=c.itself, addedBy=request.user, anonymous=anon);
      saved <- ChatCommentDAO.saveNew(cm)
    ) yield {
      EventRoom.notifyEventRoom(BroadcastIt(courseId, saved))
      Ok("")
    }
    
  }
  
  def lastFewEvents(courseId:String) = DataAction.returning.many { implicit request => 
    for {
      c <- refCourse(courseId)
      approved <- request.approval ask Permissions.Read(c.itself)
      comment <- ChatCommentDAO.lastFew(c.itself)
    } yield comment
  }

}