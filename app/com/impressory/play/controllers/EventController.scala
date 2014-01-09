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
import com.impressory.eventroom._
import com.impressory.api._
import com.impressory.api.events._
import com.impressory.security.Permissions
import com.wbillingsley.handy.appbase.DataAction
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
  
  
  def postChatMessage(courseId:String) = DataAction.returning.result(parse.json) { implicit request => 
        
    for (
      c <- refCourse(courseId);
      approved <- request.approval ask Permissions.Chat(c.itself);
      text <- Ref((request.body \ "text").asOpt[String].map(_.trim)) if (!text.isEmpty);
      anon <- Ref((request.body \ "anonymous").asOpt[Boolean].orElse(Some(false)));
      cm = ChatCommentDAO.unsaved.copy(text=text, course=c.itself, addedBy=request.user, anonymous=anon);
      saved <- ChatCommentDAO.saveNew(cm)
    ) yield {
      EventRoom.notifyEventRoom(BroadcastStandard(courseId, saved))
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