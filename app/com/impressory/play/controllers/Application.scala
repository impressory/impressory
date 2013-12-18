package com.impressory.play.controllers

import play.api._
import play.api.mvc._
import com.wbillingsley.handy._
import Ref._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.wbillingsley.handy.appbase.DataAction
import com.impressory.json.UserToJson
import com.impressory.plugins.ContentItemViews
import com.wbillingsley.handy.appbase.AppbaseRequest


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
    
    val ciMainPrefix = "viewcontent/kinds/"
    val ciEditPrefix = "editcontent/kinds/"
    val ciStreamPrefix = "viewcontent/stream/"
          
    templ match {
      case "main.html" => Ok(views.html.partials.main()) 
      case "signUp.html" => Ok(views.html.partials.signUp())
      case "logIn.html" => Ok(views.html.partials.logIn())
      case "self.html" => Ok(views.html.partials.self())
      case "about.html" => Ok(views.html.partials.about())
      
      case "course/create.html" => Ok(views.html.partials.course.create())
      case "course/editDetails.html" => Ok(views.html.partials.course.editDetails())
      case "course/cover.html" => Ok(views.html.partials.course.cover())
      case "course/invites.html" => Ok(views.html.partials.course.invites())
      case "course/activityStream.html" => Ok(views.html.partials.course.activityStream())
      case "course/index.html" => Ok(views.html.partials.course.index())
      case "course/chatRoom.html" => Ok(views.html.partials.course.chatRoom())
      case "course/viewContent.html" => Ok(views.html.partials.viewcontent.viewContent())
      case "course/embedContent.html" => Ok(views.html.partials.viewcontent.embedContent())
      
      case "qna/listQuestions.html" => Ok(views.html.partials.qna.listQuestions())
      case "qna/newQuestion.html" => Ok(views.html.partials.qna.newQuestion())
      case "qna/viewQuestion.html" => Ok(views.html.partials.qna.viewQuestion())
      
      case "viewcontent/kinds/contentSequence.html" => Ok(views.html.partials.viewcontent.kinds.contentSequence())
      case "viewcontent/kinds/markdownPage.html" => Ok(views.html.partials.viewcontent.kinds.markdownPage())
      case "viewcontent/kinds/noContent.html" => Ok(views.html.partials.viewcontent.kinds.noContent())
      
      case s if s.startsWith(ciMainPrefix) => ContentItemViews.main(s.stripPrefix(ciMainPrefix)) match {
        case Some(s) => Ok(s).as("text/html")
        case _ => NotFound("No suitable template was found")
      }

      case "viewcontent/stream/markdownPage.html" => Ok(views.html.partials.viewcontent.stream.markdownPage())
      case "viewcontent/stream/default.html" => Ok(views.html.partials.viewcontent.stream.default())
      
      case s if s.startsWith(ciStreamPrefix) => ContentItemViews.stream(s.stripPrefix(ciStreamPrefix)) match {
        case Some(s) => Ok(s).as("text/html")
        case _ => NotFound("No suitable template was found")
      }

      case "editcontent/kinds/contentSequence.html" => Ok(views.html.partials.editcontent.kinds.contentSequence())
      case "editcontent/kinds/markdownPage.html" => Ok(views.html.partials.editcontent.kinds.markdownPage())
      case "editcontent/kinds/default.html" => Ok(views.html.partials.editcontent.kinds.default())
      
      case s if s.startsWith(ciEditPrefix) => ContentItemViews.edit(s.stripPrefix(ciEditPrefix)) match {
        case Some(s) => Ok(s).as("text/html")
        case _ => NotFound("No suitable template was found")
      }

      case _ => NotFound(s"No such partial template: $templ")
    }
  }
  
}