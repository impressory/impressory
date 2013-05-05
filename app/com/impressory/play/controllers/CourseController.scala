package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay.RefConversions._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.play.model._
import ResultConversions._
import JsonConverters._
import play.api.libs.iteratee.Enumerator


object CourseController extends Controller {
  

  /** The content of a freshly created book. Updated on startup. */
  var defaultPageOneContent = "(to be loaded)";
    
  /**
   * HTML Form handler for creating a book; redirects to the book's cover page
   * @return redirects to the book's cover page
   */
  def createCourse = Action(parse.json) { implicit request =>
    val user = RequestUtils.loggedInUser(request)

  	val resp = for (
      u <- user orIfNone UserError("You're not logged in");
      approval = Approval(u.itself); 
      course <- CourseModel.createCourse(approval, request.body, ""); 
      j <- course.itself.toJson
    ) yield {
      Ok(Json.obj("course" -> j))
    }
    resp
  }

  def listedCourses = Action { implicit request => 
    val user = RequestUtils.loggedInUser(request)

    val courses = for (
        c <- Course.listedCourses;
        u <- optionally(user);
        j <- c.itself.toJsonForAppr(Approval(Ref(u)))
    ) yield j
    
    val en = Enumerator("{ \"courses\": [") andThen courses.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")
  }
  
  def myCourses = Action { implicit request => 
    val user = RequestUtils.loggedInUser(request)
    
    val courses = for (
      u <- user;
      c <- new RefManyById(classOf[Course], u.registrations.map(_._course));
      j <- c.itself.toJson
    ) yield j
      
    val en = Enumerator("{ \"courses\": [") andThen courses.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")
  }
  
  def get(cid:String) = Action { implicit request => 
    val user = RequestUtils.loggedInUser(request)
    
    val r = for (
      u <- optionally(user);
      c <- RefById(classOf[Course], cid);
      j <- c.itself.toJsonForAppr(Approval(u))
    ) yield Ok(j)
    r
    
  }


}
