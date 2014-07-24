package com.impressory.play.controllers

import com.impressory.model.CourseModel
import com.wbillingsley.handy._
import Ref._
import Ids._
import com.wbillingsley.handyplay._
import com.wbillingsley.handyplay.RefConversions._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.api.enrol._
import com.impressory.security._
import com.impressory.model._

import com.impressory.reactivemongo.{RegistrationDAO, CourseDAO, CourseInviteDAO}



object CourseController extends Controller {
  
  implicit val ctoj = com.impressory.json.CourseToJson
  implicit val citoj = com.impressory.json.CourseInviteToJson
  implicit val utoj = com.impressory.json.UserToJson
  implicit val rtoj = com.impressory.json.RegistrationToJson
  
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
      courseIds <- {
        for (r <- RegistrationDAO.byUser(u.id)) yield r.course
      }.toIds
      c <- courseIds.lookUp
    } yield c
  }
  
  /**
   * JSON for a specific course
   */
  def get(rCourse:Ref[Course]) = DataAction.returning.one {
    rCourse
  }
  
  /**
   * Updates the details of a course
   */
  def update(rCourse:Ref[Course]) = DataAction.returning.one(parse.json) { implicit request =>
    CourseModel.updateCourse(rCourse, request.approval, request.body)
  }

  /**
   * Invites for a specific course
   */
  def invites(rCourse:Ref[Course]) = { println("foo"); DataAction.returning.many { implicit request =>
    for (
      c <- rCourse;
      approved <- request.approval ask Permissions.manageCourseInvites(c.itself);
      i <- CourseInviteDAO.byCourse(c.itself)
    ) yield i
  }}
  
  /**
   * Creates an invite that will allow someone to register for a course
   */
  def createInvite(rCourse:Ref[Course]) = DataAction.returning.one(parse.json) { implicit request =>
    CourseModel.createInvite(request.approval, rCourse, request.body)
  }
  
  /**
   * Registers the logged in user for the given course using the invite code.
   */
  def useInvite(rCourse:Ref[Course]) = DataAction.returning.one(parse.json) { implicit request =>
    for {
      code <- Ref((request.body \ "code").asOpt[String]) orIfNone UserError("No code entered")
      c <- rCourse
      reg <- CourseModel.useInvite(request.approval, c.itself, code)
    } yield reg
  }

}
