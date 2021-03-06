package com.impressory.auth.controllers

import com.impressory.plugins.LookUps
import play.api.mvc.{AnyContent, Controller, Action, Request}
import play.api.libs.ws.WS
import play.api.libs.json.Json
import play.api.Play
import Play.current

import com.wbillingsley.handy._
import com.wbillingsley.handy.playoauth.{OAuthDetails, UserRecord}
import com.wbillingsley.handyplay.DataAction
import Ref._
import com.impressory.api._
import com.impressory.json._
import scala.util.Try

// At the moment, we are hardcoded in many places to use the reactivemongo DB classes
import com.impressory.reactivemongo.UserDAO

// Import the application's configuration
import com.impressory.plugins.LookUps._
import com.impressory.plugins.RouteConfig._

/**
 * Controller for the interstitial form confirming that a new account should be registered
 */
object InterstitialController extends Controller {
  
  val sessionVar = "interstitialMemory"
    
  /**
   * Handles the completion of OAuth authorisations
   */
  def onOAuth(rur:Try[OAuthDetails]) = DataAction.returning.result { implicit request =>    
    val res = for (
      mem <- rur.toRef;
      user <- optionally(userProvider.byIdentity(mem.userRecord.service, mem.userRecord.id))
    ) yield {
      user match {
        case Some(u) => {
          for (
            updated <- UserDAO.pushSession(u.itself, ActiveSession(request.sessionKey, ip = request.remoteAddress))
          ) yield Redirect(com.impressory.play.controllers.routes.Application.index)
        }         
        case None => {
          val session = request.session + (InterstitialController.sessionVar -> mem.toJson.toString) - "oauth_state"
          for (u <- optionally(request.user)) yield {
            u match {
              case Some(user) => Ok(views.html.interstitials.addOAuth(Some(mem.userRecord.service), mem.userRecord, user)).withSession(session)
              case None => Ok(views.html.interstitials.registerOAuth(Some(mem.userRecord.service), mem.userRecord)).withSession(session)
            }
          }
        }
      }
    }
    
    val flat = res.flatten    
    flat
  }
    
     
  /**
   * Register the user as a new user on this system
   */
  def registerUser = DataAction.returning.result { implicit request => 
    val memString = request.session.get(sessionVar)
    val mem = for (s <- memString; m <- (Json.parse(s) \ "userRecord").asOpt[UserRecord]) yield m
    
    for (
      details <- Ref(mem) orIfNone Refused("There appear to be no user details to register");
      user <- {
        val i = Identity(service=details.service, value=details.id, avatar=details.avatar, username=details.username);
        val u = User(
          id = LookUps.allocateId,
          name=details.name,
          nickname=details.nickname,
          avatar=details.avatar,
          identities = Seq(i),
          activeSessions = Seq(ActiveSession(request.sessionKey, ip = request.remoteAddress))
        )
        UserDAO.saveNew(u)
      }
    ) yield {
      val session = request.session - InterstitialController.sessionVar
      Redirect(com.impressory.play.controllers.routes.Application.index).withSession(session)      
    }
  }
  
  /**
   * Adds the remembered identity to the currently logged in user.
   */
  def addIdentity = DataAction.returning.result { implicit request => 
    val memString = request.session.get(sessionVar)
    val mem = for (s <- memString; m <- (Json.parse(s) \ "userRecord").asOpt[UserRecord]) yield m
    
    for {
      details <- Ref(mem) orIfNone Refused("There appear to be no user details to register");
      user <- request.user orIfNone Refused("There is no logged in user to add that identity to");
      saved <- {
        val u = user.copy(
            name=user.name orElse details.name, nickname=user.nickname orElse details.nickname, avatar=user.avatar orElse details.avatar
        )
        UserDAO.saveDetails(u)
      }
      pushed <- {
        val i = Identity(service=details.service, value=details.id, avatar=details.avatar, username=details.username);
        UserDAO.pushIdentity(user.itself, i)
      }      
    } yield {
      val session = request.session - InterstitialController.sessionVar      
      Redirect(com.impressory.play.controllers.routes.Application.index).withSession(request.session - sessionVar)      
    }
    
  }  
}