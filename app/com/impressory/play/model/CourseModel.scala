package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json.JsValue
import com.impressory.api.CourseRole

/**
 * From BookModel in the previous version
 */
object CourseModel {
  
  def update(course:Course, jsVal: JsValue) = {
    for (title <- (jsVal \ "title").asOpt[String]) { course.title = Some(title) }
    for (sn <- (jsVal \ "shortName").asOpt[String]) { course.shortName = Some(sn) }
    for (sd <- (jsVal \ "shortDescription").asOpt[String]) { course.shortDescription = Some(sd) }
    for (ld <- (jsVal \ "longDescription").asOpt[String]) { course.longDescription = Some(ld) }
    course
  }  
  
  def createCourse(approval:Approval[User], config: JsValue, pageOneContent:String):Ref[Course] = {
    for (
      a <- approval ask Permissions.CreateCourse;
      course = update(new Course(), config);
      saved <- Course.saveNew(course);
      
      p1 <- ContentEntry.unsaved(saved.itself, approval.who);
      p1saved <- {
        p1.topics = Set("page one")
        ContentEntry.saveNew(p1)
      };
      
      reg <- User.register(approval.who, saved.itself, CourseRole.values.toSet)
    ) yield saved
  }
  

}