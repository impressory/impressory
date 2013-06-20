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
import ResultConversions._
import JsonConverters._

/**
 * Controller handling subscriptions and connections to the EventRoom
 */
object EventController extends Controller {
  
  def subscribe = Action(parse.json) { implicit request => 
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
          EventRoom.default ! Subscribe(listenerName, ChatEvents.ChatStream(courseId))
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
  
  def serverSentEvents = Action { implicit request =>
    
    for (
      u <- optionally(request.user)// orIfNone RefFailed(UserError("You must be logged in to listen for updates"))
    ) yield {
      
      val listenerName = RequestUtils.newSessionKey
      val mem = Mem(u)
      val sessKey = RequestUtils.sessionKey(session).getOrElse(RequestUtils.newSessionKey)
      val context = "Direct connection"
      
      val sse = EventRoom.serverSentEvents(listenerName=listenerName, u=mem, session=sessKey, context=context)
      
      sse.withSession(RequestUtils.withSessionKey(request.session, sessKey))
    }
      
  }
  
  
  def postChatMessage(courseId:String) = Action(parse.json) { implicit request => 
    val course = RefById(classOf[Course], courseId)
    val approval = request.approval
    val sesKey = RequestUtils.sessionKey(request.session).getOrElse(RequestUtils.newSessionKey)
        
    val res = for (
      c <- course;
      approved <- approval ask Permissions.Chat(c.itself);
      text <- Ref((request.body \ "text").asOpt[String].map(_.trim)) if (!text.isEmpty);
      anon <- Ref((request.body \ "anonymous").asOpt[Boolean].orElse(Some(false)));
      cm = new ChatComment(text=text, course=c.itself, addedBy=approval.who, anonymous=anon);
      saved <- ChatComment.saveNew(cm)
    ) yield {
      EventRoom.notifyEventRoom(ChatEvents.BroadcastIt(courseId, saved))
      Ok("").withSession(RequestUtils.withSessionKey(request.session, sesKey))
    }
    res
  }
  
  def lastFewEvents(courseId:String) = Action { implicit request => 
    val course = RefById(classOf[Course], courseId)
        
    val comments = for (
      c <- course;
      approved <- request.approval ask Permissions.Read(c.itself);
      comment <- ChatComment.lastFew(c.itself)
    ) yield {
      Json.toJson(comment)
    }
    val en = Enumerator("{ \"events\": [") andThen comments.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")
  }

}