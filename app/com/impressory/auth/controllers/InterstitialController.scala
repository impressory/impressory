package com.impressory.auth.controllers

import play.api.mvc.{Controller, Action, Request}
import play.api.libs.ws.WS
import play.api.libs.json.Json
import play.api.Play
import Play.current
import com.wbillingsley.encrypt.Encrypt
import com.wbillingsley.handy._
import com.wbillingsley.handy.playoauth.UserRecord
import Ref._
import com.impressory.play.model._
import com.impressory.play.controllers.ResultConversions._
import com.impressory.play.controllers._
import play.api.mvc.AnyContent

/**
 * Controller for the interstitial form confirming that a new account should be registered
 */
object InterstitialController extends Controller {
  
  val sessionVar = "interstitialMemory"
    
  /**
   * Handles the completion of OAuth authorisations
   */
  def onOAuth(rur:Ref[UserRecord], request:Request[AnyContent]) = {    
    val res = for (
      mem <- rur;
      user <- optionally(User.byIdentity("github", mem.id))
    ) yield {
      user match {
        case Some(u) => {
          val session = RequestUtils.withLoggedInUser(request.session - "oauth_state", u.itself)
          Redirect(com.impressory.play.controllers.routes.Application.index).withSession(session)
        } 
        case None => {
          val session = request.session + (InterstitialController.sessionVar -> mem.toJsonString) - "oauth_state"
          Redirect(routes.InterstitialController.viewRegisterUser(Some("GitHub"))).withSession(session)
        }
      }
    }
    
    ResultConversions.refResultToResult(res)(request)
  }
    
  
  /**
   * Interstitial saying this account hasn't been registered yet
   */
  def viewRegisterUser(serviceName:Option[String]) = Action { implicit request => 
    val memString = request.session.get(sessionVar)
    val mem = for (s <- memString; m <- Json.parse(s).asOpt[UserRecord]) yield m
    
    val resp = for (
      details <- Ref(mem) orIfNone Refused("There appear to be no user details to register");
      u <- optionally(request.user)
    ) yield {
      u match {
        case Some(user) => Ok(views.html.interstitials.addOAuth(serviceName, details, user))
        case None => Ok(views.html.interstitials.registerOAuth(serviceName, details))
      }
    }
    resp
  }
     
  /**
   * Register the user as a new user on this system
   */
  def registerUser = Action { implicit request => 
    val memString = request.session.get(sessionVar)
    val mem = for (s <- memString; m <- Json.parse(s).asOpt[UserRecord]) yield m
    
    val resp = for (
      details <- Ref(mem) orIfNone Refused("There appear to be no user details to register");
      user <- {
        val i = Identity.unsaved(service=details.service, value=details.id, avatar=details.avatar, username=details.username);
        val u = User.unsaved(name=details.name, nickname=details.nickname, avatar=details.avatar)
        u.identities :+= i
        User.saveNew(u)
      }
    ) yield {
      val session = RequestUtils.withLoggedInUser(request.session, user.itself) - sessionVar
      Redirect(com.impressory.play.controllers.routes.Application.index).withSession(session)      
    }
    
    resp
  }
  
  /**
   * Adds the remembered identity to the currently logged in user.
   */
  def addIdentity = Action { implicit request => 
    val memString = request.session.get(sessionVar)
    val mem = for (s <- memString; m <- Json.parse(s).asOpt[UserRecord]) yield m
    
    val resp = for (
      details <- Ref(mem) orIfNone Refused("There appear to be no user details to register");
      user <- request.user orIfNone Refused("There is no logged in user to add that identity to");
      saved <- {
        val i = Identity.unsaved(service=details.service, value=details.id, avatar=details.avatar, username=details.username);
        user.identities :+= i
        user.nickname = user.nickname orElse details.nickname
        user.avatar = user.avatar orElse details.avatar
        user.name = user.name orElse details.name
        User.save(user)
      }
    ) yield {
      Redirect(com.impressory.play.controllers.routes.Application.index).withSession(request.session - sessionVar)      
    }
    
    resp
  }  
}