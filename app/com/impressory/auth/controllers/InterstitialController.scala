package com.impressory.auth.controllers

import play.api.mvc.{Controller, Action, Request}
import play.api.libs.ws.WS
import play.api.libs.json.Json
import play.api.Play
import Play.current
import com.wbillingsley.encrypt.Encrypt
import com.wbillingsley.handy._
import Ref._
import com.impressory.play.model._
import com.impressory.play.controllers.ResultConversions._
import com.impressory.play.controllers.RequestUtils

/**
 * Controller for the interstitial form confirming that a new account should be registered
 */
object InterstitialController extends Controller {
  
  val sessionVar = "interstitialMemory"
  
  /**
   * Interstitial saying this account hasn't been registered yet
   */
  def viewRegisterUser(serviceName:Option[String]) = Action { request => 
    Ok(views.html.interstitials.registerOAuth(serviceName))
  }
  
  /**
   * Register the user as a new user on this system
   */
  def registerUser = Action { implicit request => 
    val memString = request.session.get(sessionVar)
    val mem = for (s <- memString; m <- Json.parse(s).asOpt[InterstitialMemory]) yield m
    
    val resp = for (
      details <- Ref(mem) orIfNone Refused("There appear to be no user details to register");
      user <- {
        val i = Identity.unsaved(service=details.service, value=details.id, avatar=details.avatar, username=details.username);
        val u = User.unsaved(name=details.name, nickname=details.nickname, avatar=details.avatar)
        u.identities :+= i
        User.saveNew(u)
      }
    ) yield {
      val session = RequestUtils.withLoggedInUser(request.session, user.itself)
      Redirect(com.impressory.play.controllers.routes.Application.index).withSession(session)      
    }
    
    resp
  }
}