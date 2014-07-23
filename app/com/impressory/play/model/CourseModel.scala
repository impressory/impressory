package com.impressory.play.model

import com.impressory.plugins.LookUps
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.api.enrol._
import com.impressory.json._
import com.impressory.security.Permissions

import com.impressory.reactivemongo._

/**
 * From BookModel in the previous version
 */
object CourseModel {
  
  /** The content of a freshly created book. Updated on startup. */
  var defaultPageOneText = "The default content for a new course has not yet been set";  
  
  
  def createCourse(approval:Approval[User], config: JsValue, pageOneContent:String):Ref[Course] = {
    for {
      user <- approval.who

      a <- approval ask Permissions.createCourse;
      course = CourseToJson.update(Course(id=LookUps.allocateId, addedBy=user.id), config);
      saved <- CourseDAO.saveNew(course);
      
      p1 = ContentEntry(
        id = LookUps.allocateId,
        course=saved.id, addedBy=user.id, tags=CETags(topics=Set("page one")),
        settings=CESettings(published=Some(System.currentTimeMillis()))
      );
      page <- MarkdownPageModel.JsonHandler.createFromJson((MarkdownPage.itemType, Json.obj("item" -> Json.obj("text" -> defaultPageOneText)), p1));
      p1saved <- ContentEntryDAO.saveNew(page)
      
      reg = Registration(
        id = LookUps.allocateId,
        user=user.id, course=course.id, roles=CourseRole.values.toSet
      )
      updated <- RegistrationDAO.save(reg)
    } yield saved
  }
  
  def updateCourse(course:Ref[Course], approval:Approval[User], config:JsValue):Ref[Course] = {
    for (
      c <- course;
      a <- approval ask Permissions.editCourse(c.itself);
      updated = CourseToJson.update(c, config);
      saved <- CourseDAO.saveExisting(updated)
    ) yield saved
  }
  
  
  
  def createInvite(approval:Approval[User], course:Ref[Course], config: JsValue):Ref[CourseInvite] = {
    for (
      c <- course;
      u <- approval.who;
      a <- approval ask Permissions.manageCourseInvites(c.itself);
      invite = CourseInvite(
        id=LookUps.allocateId,
        course=c.id, addedBy=u.id
      );
      upd = CourseInviteToJson.update(invite, config);
      saved <- CourseInviteDAO.saveNew(upd)
    ) yield saved
  }
  
  
  def useInvite(approval:Approval[User], course:Ref[Course], code: String) = {
    for {
      c <- course
      user <- approval.who
      approved <- approval ask Permissions.registerUsingInvite(course)
      invite <- CourseInviteDAO.availableByCode(c.itself, code)
      used <- CourseInviteDAO.use(invite.itself, user.itself)
      oldReg <- optionally(RegistrationDAO.byUserAndCourse(user.id, c.id))
      updated <- oldReg match {
        case Some(r) => {
          val reg = r.copy(roles=r.roles ++ invite.roles)
          RegistrationDAO.save(reg)
        }
        case _ => {
          val reg = Registration(id=LookUps.allocateId, user=user.id, course=c.id, roles=invite.roles)
          RegistrationDAO.save(reg)
        }
      }
    } yield updated
  }
  

}