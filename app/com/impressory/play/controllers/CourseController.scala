package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.api.enrol._
import com.impressory.security._
import com.impressory.play.model._
import com.wbillingsley.handy.appbase.DataAction

import com.impressory.reactivemongo.{CourseDAO, CourseInviteDAO}



object CourseController extends Controller {
  
  implicit val ctoj = com.impressory.json.CourseToJson
  implicit val citoj = com.impressory.json.CourseInviteToJson
  implicit val utoj = com.impressory.json.UserToJson
  
  import com.impressory.plugins.LookUps._
  import com.impressory.plugins.RouteConfig._
    
  /**
   * Handler for creating a book
   * { course: { ... } }
   */
  def createCourse = DataAction.returning.one(parse.json) { implicit request =>    
    CourseModel.createCourse(request.approval, request.body, "")
  }

  /**
   * Courses for which the "listed" flag has been set
   * { courses: [ ... ] }
   */
  def listedCourses = DataAction.returning.many { implicit request => 
    CourseDAO.listedCourses
  }
  
  /**
   * Courses for which the logged in user is registered, in JSON format
   * { courses: [ ... ] }
   */
  def myCourses = DataAction.returning.many { implicit request => 
    for {
      u <- request.user
      courseIds = {
        println(u.registrations)
        for (r <- u.registrations; id <- r.course.getId) yield id
      }
      c <- RefManyById.of[Course](courseIds)
    } yield c
  }
  
  /**
   * JSON for a specific course
   */
  def get(cid:String) = DataAction.returning.one { 
    refCourse(cid)
  }
  
  /**
   * Updates the details of a course
   */
  def update(cid:String) = DataAction.returning.one(parse.json) { implicit request =>
    CourseModel.updateCourse(refCourse(cid), request.approval, request.body)
  }

  /**
   * Invites for a specific course
   */
  def invites(cid: String) = { println("foo"); DataAction.returning.many { implicit request => 
    for (
      c <- refCourse(cid);
      approved <- request.approval ask Permissions.ManageCourseInvites(c.itself);
      i <- CourseInviteDAO.byCourse(c.itself)
    ) yield i
  }}
  
  /**
   * Creates an invite that will allow someone to register for a course
   */
  def createInvite(cid:String) = DataAction.returning.one(parse.json) { implicit request => 
    CourseModel.createInvite(request.approval, refCourse(cid), request.body)
  }
  
  /**
   * Registers the logged in user for the given course using the invite code.
   */
  def useInvite(cid:String) = DataAction.returning.one(parse.json) { implicit request => 
    for {
      code <- Ref((request.body \ "code").asOpt[String]) orIfNone UserError("No code entered")
      c <- refCourse(cid)
      reg <- CourseModel.useInvite(request.approval, c.itself, code)
    } yield reg
  }

}
