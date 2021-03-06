package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay._
import com.wbillingsley.handyplay.RefConversions._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import com.impressory.api._
import com.impressory.plugins._
import com.impressory.json._
import com.impressory.reactivemongo.UserDAO


object UserController extends Controller {

  import com.impressory.plugins.LookUps._
  import com.impressory.plugins.RouteConfig._
  
  implicit val utoj = com.impressory.json.UserToJson
  
  /**
   * Checks whether an email address is valid.
   * To be replaced with something more comprehensive!
   */
  def valid(email:String) = email.contains("@")
   
  def user(rU:Ref[User]) = DataAction.returning.one {
    rU
  }
  
  /**
   * JSON for the currently logged in user
   */
  def whoAmI = DataAction.returning.one { implicit request => 
    request.user
  }
  
  def findUsersById = DataAction.returning.many(parse.json) { implicit request =>
    val ids = (request.body \ "ids").asOpt[Set[String]].getOrElse(Set.empty)
    RefManyById.of[User](ids.toSeq)
  }
  
  /**
   * log out action
   */
  def logOut = DataAction.returning.one { implicit request => 
    UserDAO.deleteSession(request.user, ActiveSession(request.sessionKey, ip=request.remoteAddress))
  }
  
  /**
   * sign up using email address and password action
   */
  def signUp = DataAction.returning.one(parse.json) { implicit request =>   
    for (
      email <- Ref((request.body \ "email").asOpt[String]) orIfNone UserError("Email must not be blank");
      password <- Ref((request.body \ "password").asOpt[String]) orIfNone UserError("Password must not be blank");
      user <- {
        val p = new PasswordLogin(email=Some(email))
        val set = User(
          id = LookUps.allocateId,
          nickname=Some(email.takeWhile(_ != '@')),
          pwlogin=p.copy(pwhash=Some(p.hash(password))),
          activeSessions=Seq(ActiveSession(request.sessionKey, ip=request.remoteAddress))
        )
        UserDAO.saveNew(set)
      }      
    ) yield user  
  }
  
  /**
   * login using username and password action
   */
  def logInUP = DataAction.returning.one(parse.json) { implicit request =>   
    val username = (request.body \ "username").asOpt[String]
    val password = (request.body \ "password").asOpt[String] 
    
    val resp = for {
        un <- Ref(username);
        pw <- Ref(password);
        u <- UserDAO.byUsername(un) if (u.pwlogin.pwhash == Some(u.pwlogin.hash(pw))); 
        pushed <- UserDAO.pushSession(u.itself, ActiveSession(request.sessionKey, ip=request.remoteAddress))
    } yield pushed 
    
    resp orIfNone(RefFailed(UserError("Wrong password")))
  }
  
  /**
   * login using email address and password action
   */
  def logInEP = DataAction.returning.one(parse.json) { implicit request =>   
    val email = (request.body \ "email").asOpt[String]
    val password = (request.body \ "password").asOpt[String] 
    
    val resp = for {
        un <- Ref(email);
        pw <- Ref(password);
        u <- UserDAO.byEmail(un) if (u.pwlogin.pwhash == u.pwlogin.hash(pw));
        pushed <- UserDAO.pushSession(u.itself, ActiveSession(request.sessionKey, ip=request.remoteAddress))        
    } yield pushed
    
    resp orIfNone(RefFailed(UserError("Wrong password")))
  }  
  
  /**
   * Action for editing basic details on the logged in user
   */
  def editDetails = DataAction.returning.one(parse.json) { implicit request =>
    for {
        u <- request.user
        updated <- UserDAO.saveDetails(u.copy(
          nickname = (request.body \ "nickname").asOpt[String] orElse u.nickname,
          name = (request.body \ "name").asOpt[String] orElse u.name,
          avatar = (request.body \ "avatar").asOpt[String] orElse u.avatar
        ))
    } yield updated
  }
  
  /**
   * Action for editing basic details on the logged in user
   */
  def editLoginDetails = DataAction.returning.one(parse.json) { implicit request =>
    for {
      u <- request.user
      updated <- UserDAO.updatePWLogin(u.copy(
        pwlogin = u.pwlogin.copy(
          email = (request.body \ "email").asOpt[String] orElse u.pwlogin.email,
          username = (request.body \ "username").asOpt[String] orElse u.pwlogin.username
        )
      ))
    } yield updated
  }
  
  /**
   * Changing the user's password
   */
  def changePassword = DataAction.returning.one(parse.json) { implicit request => 
    for {
      u <- request.user
      newPassword <- (request.body \ "newPassword").asOpt[String].toRef orIfNone UserError("No new password!")
      oldHash = (request.body \ "oldPassword").asOpt[String].map(u.pwlogin.hash(_))
      matches <- u.pwlogin.pwhash match {
        case Some(hash) => if (Some(hash) == oldHash) {
          true.itself
        } else {
          RefFailed(UserError("Old password did not match"))
        }
        case None => true.itself
      }
      updated <- UserDAO.setPassword(u, newPassword)
    } yield updated
  }
  
  /**
   * A username is available if nobody has it, or the logged in user has it
   */
  def usernameAvailable(username:String) = DataAction.returning.json { implicit request =>     
    val taken = for {
      userId <- optionally(request.user.refId)
      u <- UserDAO.byUsername(username)
    } yield Json.obj("available" -> (u.itself.getId == userId))
    taken orIfNone Json.obj("available" -> true).itself
  }

  /**
   * A username is "available" if nobody has it, or the logged in user has it
   */
  def emailAvailable(email:String) = DataAction.returning.json { implicit request => 
    val taken = for {
      userId <- optionally(request.user.refId)
      u <- UserDAO.byEmail(email)
    } yield Json.obj("available" -> (u.itself.getId == userId))
    taken orIfNone Json.obj("available" -> true).itself
  }
  
  
  /**
   * Removes an identity from the logged in user
   */
  def removeIdentity = DataAction.returning.one(parse.json) { implicit request =>
    for {
      s <- (request.body \ "service").asOpt[String].toRef
      i <- (request.body \ "id").asOpt[String].toRef
      u <-UserDAO.deleteIdentity(request.user, s, i) 
    } yield u
  }
}