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
  
    
  /**
   * Handler for creating a book
   * { course: { ... } }
   */
  def createCourse = Action(parse.json) { implicit request =>
    
    val resp = for (
      course <- CourseModel.createCourse(request.approval, request.body, ""); 
      j <- course.itself.toJson
    ) yield {
      Ok(Json.obj("course" -> j))
    }
    resp
  }

  /**
   * Courses for which the "listed" flag has been set
   * { courses: [ ... ] }
   */
  def listedCourses = Action { implicit request => 

    val courses = for (
        c <- Course.listedCourses;
        j <- c.itself.toJsonForAppr(request.approval)
    ) yield j
    
    val en = Enumerator("{ \"courses\": [") andThen courses.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")
  }
  
  /**
   * Courses for which the logged in user is registered, in JSON format
   * { courses: [ ... ] }
   */
  def myCourses = Action { implicit request => 
    
    val courses = for (
      u <- request.user;
      courseIds = {
        for (r <- u.registrations; id <- r.course.getId) yield id
      };
      c <- new RefManyById(classOf[Course], courseIds);
      j <- c.itself.toJson
    ) yield j
      
    val en = Enumerator("{ \"courses\": [") andThen courses.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")
  }
  
  /**
   * JSON for a specific course
   */
  def get(cid:String) = Angular { Action { implicit request => 
    val r = for (
      c <- refCourse(cid);
      j <- c.itself.toJsonForAppr(request.approval)
    ) yield Ok(j)
    r
  }}
  
  /**
   * Updates the details of a course
   */
  def update(cid:String) = Action(parse.json) { implicit request =>
    val approval = request.approval
    val r = for (
      updated <- CourseModel.updateCourse(refCourse(cid), request.approval, request.body);
      j <- updated.itself.toJsonForAppr(approval)
    ) yield j
    r
  }

  /**
   * Invites for a specific course
   */
  def invites(cid: String) = Angular { Action { implicit request => 
    
    val invites = for (
      c <- refCourse(cid);
      approved <- request.approval ask Permissions.ManageCourseInvites(c.itself);
      is <- CourseInvite.byCourse(c.itself);
      j <- is.itself.toJson
    ) yield j
    invites
  }}
  
  /**
   * Creates an invite that will allow someone to register for a course
   */
  def createInvite(cid:String) = Action(parse.json) { implicit request => 

    val r = for (
      ci <- CourseModel.createInvite(request.approval, refCourse(cid), request.body);
      j <- ci.itself.toJson
    ) yield Ok(j)
    r
  }
  
  /**
   * Registers the logged in user for the given course using the invite code.
   */
  def useInvite(cid:String) = Action(parse.json) { implicit request => 

    val r = for (
      code <- Ref((request.body \ "code").asOpt[String]) orIfNone UserError("No code entered");
      c <- refCourse(cid);
      reg <- CourseModel.useInvite(request.approval, c.itself, code);
      j <- reg.itself.toJson
    ) yield Ok(j)
    r
  }

}
