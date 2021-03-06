package com.impressory.play.controllers

import play.api._
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator}
import play.api.mvc._
import com.wbillingsley.handy._
import Ref._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.wbillingsley.handyplay.DataAction
import com.impressory.json.UserToJson
import com.impressory.plugins._
import com.wbillingsley.handyplay.AppbaseRequest


object Application extends Controller {  
  
  import com.impressory.plugins.LookUps._
  import com.impressory.plugins.RouteConfig._
  
  def inspectSession = Action { request =>    
    import play.api.libs.json._
    Ok(Json.toJson(request.session.data))
  }
  
  def printItOut = Action { request => 
    println("---Request received for printing---")
    println("Headers:")
    println(request.headers)
    println("\nBody:")
    println(request.toString)
    Ok("")
  }
    
  def chunkedFailure = Action {
    val enum = Enumerator(5, 4, 3, 2, 1, 0)
    val iter = Enumeratee.map[Int] { x => 100/x }
    val str = Enumeratee.map[Int] { x => s"${100/x}," }
    val recovery = Enumeratee.recover[String] { (error, input) =>
      error.printStackTrace
      Logger.error("Failed during enumeration", error)
    }
    Results.Ok.chunked(enum.through(str).andThen(Enumerator("finished")))
  }
  
  def index = DataAction.returning.result { implicit request =>
    for {
      j <- optionally(request.user.flatMap(UserToJson.toJsonForSelf(_)))
    } yield Ok(views.html.main(j))  
  }
  
  def indexInner(request:RequestHeader) = {
    for {
      j <- optionally(userProvider.user(request).flatMap(UserToJson.toJsonForSelf(_)))
    } yield Ok(views.html.main(j))
  }

  /**
   * We put the partial templates into this method so that adding a partial 
   * template does not require editing the routes file. 
   * 
   * Editing the routes file would (in dev mode) cause a complete recompilation
   * of all sources, which takes much longer than just recompiling this controller
   */
  def partial(templ:String) = Action { 
          
    templ match {
      case "main.html" => Ok(views.html.partials.main()) 
      case "signUp.html" => Ok(views.html.partials.signUp())
      case "logIn.html" => Ok(views.html.partials.logIn())
      case "about.html" => Ok(views.html.partials.about())
      
      case "course/create.html" => Ok(views.html.partials.course.create())
      case "course/editDetails.html" => Ok(views.html.partials.course.editDetails())
      case "course/cover.html" => Ok(views.html.partials.course.cover())
      case "course/invites.html" => Ok(views.html.partials.course.invites())
      case "course/activityStream.html" => Ok(views.html.partials.course.activityStream())
      case "course/index.html" => Ok(views.html.partials.course.index())
      case "course/myDrafts.html" => Ok(views.html.partials.course.myDrafts())
      case "course/chatRoom.html" => Ok(views.html.partials.course.chatRoom())
      case "course/viewContent.html" => Ok(views.html.partials.viewcontent.viewContent())
      case "course/embedContent.html" => Ok(views.html.partials.viewcontent.embedContent())
      
      case "user/self.html" => Ok(views.html.partials.user.self())

      case _ => NotFound(s"No such partial template: $templ")
    }
  }

  def editPartial(k:String) = Action {
    ContentItemViews.edit(k) match {
      case Some(s) => Ok(s).as("text/html")
      case _ => NotFound("No suitable template was found")
    }
  }
  
  def mainPartial(k:String) = Action {
    ContentItemViews.main(k) match {
      case Some(s) => Ok(s).as("text/html")
      case _ => Ok(views.html.partials.viewcontent.kinds.unrecognisedContent())
    }
  }
  
  def streamPartial(k:String) = Action {
    ContentItemViews.stream(k) match {
      case Some(s) => Ok(s).as("text/html")
      case _ => Ok(views.html.partials.viewcontent.stream.default())
    }
  }
  
  /*
   * Templates for rendering events in the chat room
   */
  def eventPartial(k:String) = Action { 
    EventViews.view(k) match {
      case Some(s) => Ok(s).as("text/html")
      case _ => NotFound("No suitable template was found")
    }
  }

  def loginServices = DataAction.returning.json { implicit request =>
    play.api.libs.json.Json.toJson(
      for {
        service <- com.wbillingsley.handy.playoauth.PlayAuth.enabledServices
      } yield service.name
    ).itself
  }

}