package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.api.enrol._
import com.impressory.json._
import com.impressory.security.Permissions

import com.impressory.reactivemongo.{CourseDAO, UserDAO, ContentEntryDAO, CourseInviteDAO}

/**
 * From BookModel in the previous version
 */
object CourseModel {
  
  /** The content of a freshly created book. Updated on startup. */
  var defaultPageOneText = "The default content for a new course has not yet been set";  
  
  
  def createCourse(approval:Approval[User], config: JsValue, pageOneContent:String):Ref[Course] = {
    for {
      a <- approval ask Permissions.CreateCourse;
      course = CourseToJson.update(CourseDAO.unsaved, config);
      saved <- CourseDAO.saveNew(course);
      
      p1 = ContentEntryDAO.unsaved.copy(course=saved.itself, addedBy=approval.who);
      page <- MarkdownPageModel.createFromJson(p1, Json.obj());
      p1saved <- ContentEntryDAO.saveNew(p1)
      
      reg = Registration(course=course.itself, roles=CourseRole.values.toSet)
      updated <- UserDAO.pushRegistration(approval.who, reg)
    } yield saved
  }
  
  def updateCourse(course:Ref[Course], approval:Approval[User], config:JsValue):Ref[Course] = {
    for (
      c <- course;
      a <- approval ask Permissions.EditCourse(c.itself);
      updated = CourseToJson.update(c, config);
      saved <- CourseDAO.saveExisting(updated)
    ) yield saved
  }
  
  
  
  def createInvite(approval:Approval[User], course:Ref[Course], config: JsValue):Ref[CourseInvite] = {
    for (
      c <- course;
      a <- approval ask Permissions.ManageCourseInvites(c.itself);
      invite = CourseInviteDAO.unsaved.copy(course=course, addedBy=approval.who);
      upd = CourseInviteToJson.update(invite, config);
      saved <- CourseInviteDAO.saveNew(upd)
    ) yield saved
  }
  
  
  def useInvite(approval:Approval[User], course:Ref[Course], code: String) = {
    for {
      c <- course
      approved <- approval ask Permissions.RegisterUsingInvite(course)
      invite <- CourseInviteDAO.availableByCode(c.itself, code)
      used <- CourseInviteDAO.use(invite.itself, approval.who)
      reg = Registration(course=course, roles=invite.roles)
      updated <- UserDAO.pushRegistration(approval.who, reg)
    } yield updated
  }
  

}