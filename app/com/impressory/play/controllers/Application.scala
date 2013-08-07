package com.impressory.play.controllers

import play.api._
import play.api.mvc._
import com.wbillingsley.handy._
import Ref._
import ResultConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.impressory.play.json.JsonConverters._

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
    import com.impressory.play.json.UserToJson._
    
    for (
        u <- optionally(request.user.toJsonForSelf)
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
      case "viewcontent/kinds/googleSlides.html" => Ok(views.html.partials.viewcontent.kinds.googleSlides())
      case "viewcontent/kinds/markdownPage.html" => Ok(views.html.partials.viewcontent.kinds.markdownPage())
      case "viewcontent/kinds/multipleChoicePoll.html" => Ok(views.html.partials.viewcontent.kinds.multipleChoicePoll())
      case "viewcontent/kinds/webPage.html" => Ok(views.html.partials.viewcontent.kinds.webPage())
      case "viewcontent/kinds/youTubeVideo.html" => Ok(views.html.partials.viewcontent.kinds.youTubeVideo())
      case "viewcontent/kinds/noContent.html" => Ok(views.html.partials.viewcontent.kinds.noContent())
      
      case "viewcontent/stream/youTubeVideo.html" => Ok(views.html.partials.viewcontent.stream.youTubeVideo())
      case "viewcontent/stream/markdownPage.html" => Ok(views.html.partials.viewcontent.stream.markdownPage())
      case "viewcontent/stream/default.html" => Ok(views.html.partials.viewcontent.stream.default())
      
      
      case _ => NotFound(s"No such partial template: $templ")
    }
  }
  
}