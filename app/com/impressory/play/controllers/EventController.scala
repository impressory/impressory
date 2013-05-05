package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._

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

/**
 * Controller handling subscriptions and connections to the EventRoom
 */
object EventController extends Controller {
  
  def subscribe = Action(parse.json) { implicit request => 
    val user = RequestUtils.loggedInUser(request)
    
    val resp = (request.body \ "subscription" \ "type").asOpt[String] match {
      
      case _ => Ok(Json.obj("error" -> "Nothing to subscribe to")).itself 
    }
    resp
  }
  
  def serverSentEvents = Action { implicit request =>
    val user = RequestUtils.loggedInUser(request)
    
    for (
      u <- optionally(user)// orIfNone RefFailed(UserError("You must be logged in to listen for updates"))
    ) yield {
      
      val listenerName = RequestUtils.newSessionKey
      val mem = Mem(u)
      val sessKey = RequestUtils.sessionKey(session).getOrElse(RequestUtils.newSessionKey)
      val context = "Direct connection"
      
      val sse = EventRoom.serverSentEvents(listenerName=listenerName, u=mem, session=sessKey, context=context)
      
      sse.withSession(RequestUtils.withSessionKey(request.session, sessKey))
    }
      
  }

}