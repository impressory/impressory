package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.play.model._
import ResultConversions._
import JsonConverters._
import play.api.libs.iteratee.Enumerator

object UserController extends Controller {
  
  /**
   * Checks whether an email address is valid.
   * To be replaced with something more comprehensive!
   */
  def valid(email:String) = email.contains("@")
   
  def user(id:String) = Action { implicit request =>
    refUser(id)
  }
  
  def findUsersById = Action(parse.json) { implicit request =>
    
    val ids = (request.body \ "ids").asOpt[Set[String]].getOrElse(Set.empty)
	val users = for (
	    user <- new RefManyById(classOf[User], ids.toSeq);
	    j <- user.itself.toJson
	) yield j

	val en = Enumerator("{ \"users\": [") andThen users.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")
  }
  
  /**
   * log out action
   */
  def logOut = Action { implicit request => 
    val session = RequestUtils.withLoggedInUser(request.session, RefNone)         
    Ok("").withSession(session)  
  }
  
  /**
   * sign up using email address and password action
   */
  def signUp = Angular { Action(parse.json) { implicit request =>   
    val email = (request.body \ "email").asOpt[String] getOrElse { throw UserError("Email cannot be empty") }
    val password = (request.body \ "password").asOpt[String] getOrElse { throw UserError("Password cannot be empty") } 
    
    if (!valid(email)) throw UserError("Hey, that doesn't look like a valid email address")
    
    val resp = for (
      u <- User.createByEmail(email, password);
      j <- u.itself.toJsonForSelf
    ) yield {
      val session = RequestUtils.withLoggedInUser(request.session, u.itself)         
      Ok(Json.obj("user" -> j)).withSession(session)
    }
    resp
  }}
  
  /**
   * login using username and password action
   */
  def logInUP = Action(parse.json) { implicit request =>   
    val username = (request.body \ "username").asOpt[String]
    val password = (request.body \ "password").asOpt[String] 
    
    val resp = (for (
        un <- Ref(username);
        pw <- Ref(password);
        u <- User.byUsername(un) if (u.pwhash == Some(u.hash(pw))); 
        j <- u.itself.toJsonForSelf
     ) yield {
      val session = RequestUtils.withLoggedInUser(request.session, u.itself)         
      Ok(Json.obj("user" -> j)).withSession(session)
    }).orIfNone(RefFailed(UserError("Wrong password")))
    
    resp        
  }
  
  /**
   * login using email address and password action
   */
  def logInEP = Action(parse.json) { implicit request =>   
    val email = (request.body \ "email").asOpt[String]
    val password = (request.body \ "password").asOpt[String] 
    
    val resp = (for (
        un <- Ref(email);
        pw <- Ref(password);
        u <- User.byEmail(un) if (u.pwhash == u.hash(pw)); 
        j <- u.itself.toJsonForSelf        
     ) yield {
      val session = RequestUtils.withLoggedInUser(request.session, u.itself)         
      Ok(Json.obj("user" -> j)).withSession(session)
    }).orIfNone(RefFailed(UserError("Wrong password")))
    
    resp        
  }  
  
  /**
   * Action for editing basic details on the logged in user
   */
  def editDetails = Action(parse.json) { implicit request =>
    
    val resp = (for (
        u <- request.user
    ) yield {
      u.nickname = (request.body \ "nickname").asOpt[String]
      u.name = (request.body \ "name").asOpt[String]
      u.avatar = (request.body \ "avatar").asOpt[String]
      
      for (j <- u.save.toJsonForSelf) yield Json.obj("user" -> j)
      
    }).flatten
    resp
  }
  
  /**
   * Action for editing basic details on the logged in user
   */
  def editLoginDetails = Action(parse.json) { implicit request =>
    
    val resp = (for (
        u <- request.user
    ) yield {
      u.email = (request.body \ "email").asOpt[String]
      u.username = (request.body \ "username").asOpt[String]
      
      for (j <- u.save.toJsonForSelf) yield Json.obj("user" -> j)
      
    }).flatten
    resp
  }  
  
  /**
   * A username is available if nobody has it, or the logged in user has it
   */
  def usernameAvailable(username:String) = Action { implicit request =>     
    val loggedIn = request.user    
  	val resp = (for (u <- User.byUsername(username)) yield {
  	  Ok(Json.obj("available" -> (u.itself.getId == loggedIn.getId)))
  	}).orIfNone(Ok(Json.obj("available" -> true)).itself)
  	resp
  }

  /**
   * A username is "available" if nobody has it, or the logged in user has it
   */
  def emailAvailable(email:String) = Action { implicit request => 
    val loggedIn = request.user    
  	val resp = (for (u <- User.byEmail(email)) yield {
  	  Ok(Json.obj("available" -> (u.itself.getId == loggedIn.getId)))
  	}).orIfNone(Ok(Json.obj("available" -> true)).itself)
  	resp
  }
  
  
  /**
   * Removes an identity from the logged in user
   */
  def removeIdentity(id: String) = Action { implicit request =>     
    val ur = request.user
  	val resp = (for (u <- ur) yield {
  	  u.identities = u.identities.filter(_._id.stringify != id)
      for (j <- u.save.toJsonForSelf) yield Ok(Json.obj("user" -> j))
  	}).flatten
  	resp
  }
}