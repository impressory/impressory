package com.impressory.play.controllers

import play.api._
import play.api.mvc._
import com.wbillingsley.handy._
import Ref._
import ResultConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.impressory.play.model.JsonConverters._

object Application extends Controller {  
  
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
    

  
  def index = Action { implicit request => 
    angularMain    
  }
  
  /*--
   * Routes that are only used at the client and should be redirected to index:
   *--*/
  def viewLogIn = index
  def viewSelf = index
  def viewCreateGroup = index
  def viewGroup(id:String) = index
  def viewCommand = index
  
  
  
  def angularMain(implicit request:Request[_]) = {
    for (
        u <- optionally(RequestUtils.loggedInUser(request).toJson)
    ) yield {
      Ok(views.html.main(u))  
    }
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
      case "self.html" => Ok(views.html.partials.self())
      
      case "course_create.html" => Ok(views.html.partials.course.create())
      case "course_cover.html" => Ok(views.html.partials.course.cover())
      case "course_listContent.html" => Ok(views.html.partials.course.listContent())
      case "course_viewContent.html" => Ok(views.html.partials.viewcontent.viewContent())
      case _ => NotFound(s"No such partial template: $templ")
    }
  }
  
}