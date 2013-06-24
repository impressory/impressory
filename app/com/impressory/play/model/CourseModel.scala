package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json.JsValue
import com.impressory.api.CourseRole

/**
 * From BookModel in the previous version
 */
object CourseModel {
  
  /** The content of a freshly created book. Updated on startup. */
  var defaultPageOneText = "The default content for a new course has not yet been set";  
  
  def update(course:Course, jsVal: JsValue) = {
    for (title <- (jsVal \ "title").asOpt[String]) { course.title = Some(title) }
    for (sn <- (jsVal \ "shortName").asOpt[String]) { course.shortName = Some(sn) }
    for (sd <- (jsVal \ "shortDescription").asOpt[String]) { course.shortDescription = Some(sd) }
    for (ld <- (jsVal \ "longDescription").asOpt[String]) { course.longDescription = Some(ld) }
    for (ld <- (jsVal \ "coverImageURL").asOpt[String]) { course.coverImageURL = Some(ld) }
    for (ld <- (jsVal \ "listed").asOpt[Boolean]) { course.listed = ld }
    course
  }  
  
  def createCourse(approval:Approval[User], config: JsValue, pageOneContent:String):Ref[Course] = {
    for (
      a <- approval ask Permissions.CreateCourse;
      course = update(new Course(), config);
      saved <- Course.saveNew(course);
      
      p1 <- ContentEntry.unsaved(saved.itself, approval.who);
      page <- MarkdownPageModel.create(course.itself, approval, p1, defaultPageOneText);
      p1saved <- {
        p1.topics = Set("page one")
        p1.kind = Some(MarkdownPage.itemType)
        p1.item = Some(page)
        ContentEntry.saveNew(p1)
      };
      
      reg <- User.register(approval.who, saved.itself, CourseRole.values.toSet)
    ) yield saved
  }
  
  def updateCourse(course:Ref[Course], approval:Approval[User], config:JsValue):Ref[Course] = {
    for (
      c <- course;
      a <- approval ask Permissions.EditCourse(c.itself);
      updated = update(c, config);
      saved <- Course.saveExisting(updated)
    ) yield saved
  }
  
  
  def updateInvite(ci:CourseInvite, jsVal:JsValue) = {
    for (code <- (jsVal \ "code").asOpt[String] if !code.trim().isEmpty) { ci.code = code.trim() }
    for (limitedNum <- (jsVal \ "limitedNumber").asOpt[Boolean]) { ci.limitedNumber = limitedNum }
    for (remaining <- (jsVal \ "remaining").asOpt[Int]) { ci.remaining = remaining }
    for (
      (role, value) <- (jsVal \ "roles").asOpt[Map[String, Boolean]].getOrElse(Map.empty) 
    ) {
      if (value) {
        ci.roles += CourseRole.valueOf(role)
      } else {
        ci.roles -= CourseRole.valueOf(role)
      }
    }
    ci
  }
  
  def createInvite(approval:Approval[User], course:Ref[Course], config: JsValue):Ref[CourseInvite] = {
    for (
      c <- course;
      a <- approval ask Permissions.ManageCourseInvites(c.itself);
      invite = new CourseInvite(course, addedBy = approval.who);
      upd = updateInvite(invite, config);
      saved <- CourseInvite.saveNew(upd)
    ) yield saved
  }
  
  
  def useInvite(approval:Approval[User], course:Ref[Course], code: String) = {
    for (
      c <- course;
      approved <- approval ask Permissions.RegisterUsingInvite(course);
      invite <- CourseInvite.availableByCode(c.itself, code);
      used <- CourseInvite.use(invite.itself, approval.who);
      reg <- User.register(approval.who, course, invite.roles)
    ) yield reg
  }
  

}