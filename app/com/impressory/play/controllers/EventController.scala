package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay._
import com.wbillingsley.eventroom.Subscribe

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Enumeratee
import play.api.libs.iteratee.Iteratee

import play.api.http.HeaderNames

import com.impressory.model._
import com.impressory.eventroom._
import com.impressory.api._
import com.impressory.api.events._
import com.impressory.plugins._
import com.impressory.security.Permissions

import com.impressory.reactivemongo.ChatCommentDAO
import com.impressory.eventroom.EventRoom

/**
 * Controller handling subscriptions and connections to the EventRoom
 */
object EventController extends Controller {
  
  import com.impressory.plugins.LookUps._
  import com.impressory.plugins.RouteConfig._
  
  implicit val cctoj = com.impressory.json.ChatCommentToJson
  
  def subscribe = DataAction.returning.result(parse.json) { implicit request => 
    for {
      listenerName <- (request.body \ "listenerName").asOpt[String].toRef orIfNone UserError("No listener to subscribe")
      subscription <- (request.body \ "subscription").asOpt[JsObject].toRef orIfNone UserError("Nothing to subscribe to")
      lt <- EventRoom.listenToFromJson(subscription, request.approval)
    } yield {
      EventRoom.default ! Subscribe(listenerName, lt)
      Ok(Json.obj("ok" -> "ok"))        
    }
  }
  
  def serverSentEvents = DataAction.returning.result { implicit request =>
    
    for (
      u <- optionally(request.user)// orIfNone RefFailed(UserError("You must be logged in to listen for updates"))
    ) yield {
      val mem = Mem(u)
      val context = "Direct connection"
      val uuid = java.util.UUID.randomUUID().toString()
      EventRoom.serverSentEvents(listenerName=uuid, u=mem, session=request.sessionKey, context=context)
    }      
  }
  
  
  def postChatMessage(rCourse:Ref[Course]) = DataAction.returning.result(parse.json) { implicit request =>
        
    for {
      c <- rCourse;
      user <- optionally(request.approval.who)
      userId = user.map(_.id)
      approved <- request.approval ask Permissions.chat(c.itself);
      text <- Ref((request.body \ "text").asOpt[String].map(_.trim)) if (!text.isEmpty);
      anon <- Ref((request.body \ "anonymous").asOpt[Boolean].orElse(Some(false)));
      cm = ChatComment(id=LookUps.allocateId, text=text, course=c.id, addedBy=userId, anonymous=anon);
      saved <- ChatCommentDAO.saveNew(cm)
    } yield {
      EventRoom.notifyEventRoom(BroadcastStandard(c.id, saved))
      Ok("")
    }
    
  }
  
  def lastFewEvents(rCourse:Ref[Course]) = DataAction.returning.many { implicit request =>
    for {
      c <- rCourse
      approved <- request.approval ask Permissions.readCourse(c.itself)
      comment <- ChatCommentDAO.lastFew(c.itself)
    } yield comment
  }

}