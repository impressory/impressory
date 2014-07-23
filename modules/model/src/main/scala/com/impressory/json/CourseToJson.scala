package com.impressory.json

import com.impressory.api._
import com.impressory.api.enrol._
import com.impressory.plugins.LookUps
import com.impressory.security._

import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.RefMany._
import com.wbillingsley.handyplay.JsonConverter
import play.api.libs.json._


object CourseToJson extends JsonConverter[Course, User]{

  implicit object signupPolicyFormat extends Format[CourseSignupPolicy] {
    def reads(j:JsValue) = {
      JsSuccess(CourseSignupPolicy.valueOf(j.as[String]))
    }

    def writes(csp:CourseSignupPolicy) = JsString(csp.toString)
  }

  implicit object chatPolicyFormat extends Format[CourseChatPolicy] {
    def reads(j:JsValue) = {
      JsSuccess(CourseChatPolicy.valueOf(j.as[String]))
    }

    def writes(csp:CourseChatPolicy) = JsString(csp.toString)
  }

  implicit val cctToJson = Json.writes[CourseContentTags]

  implicit val cmFormat = Json.format[CoverMatter]

  implicit val ltiFormat = Json.format[LTIData]

  implicit val settingsFormat = Json.format[CourseSettings]



  /**
   * Raw JSON for a course
   */
  def toJson(course: Course) = {
    Json.obj(
      "id" -> course.id,
      "coverMatter" -> course.coverMatter,
      "contentTags" -> course.contentTags,
      "settings" -> course.settings
    ).itself
  }

  /**
   * JSON for a Course, including registration and permission
   * information for this User.
   */
  def toJsonFor(course: Course, appr: Approval[User]) = {

    // Registrations. Note, can produce RefNone
    val reg = for {
      u <- appr.who
      r <- LookUps.registrationProvider.find(u.id, course.id)
      j <- RegistrationToJson.toJson(r)
    } yield j

    // Permissions.
    val perms = for {
      read <- appr askBoolean Permissions.readCourse(course.itself)
      chat <- appr askBoolean Permissions.chat(course.itself)
      edit <- appr askBoolean Permissions.editCourse(course.itself)
      add <- appr askBoolean Permissions.addContent(course.itself)
    } yield Json.obj(
      "read" -> read,
      "add" -> add,
      "chat" -> chat,
      "edit" -> edit
    )

    // Combine the JSON responses, noting that reg or perms might be RefNone
    for {
      j <- toJson(course)
      r <- optionally(reg); p <- optionally(perms)
    } yield j ++ Json.obj(
      "registration" -> r,
      "permissions" -> p
    )

  }
  
  
  def update(course:Course, jsVal: JsValue) = {
    course.copy(
      coverMatter = (jsVal \ "coverMatter").asOpt[CoverMatter] getOrElse course.coverMatter,
      settings = (jsVal \ "settings").asOpt[CourseSettings] getOrElse course.settings
    )
  }    
}

object CourseInviteToJson extends JsonConverter[CourseInvite, User] {
  
  def toJson(ci: CourseInvite) = Json.obj(
    "code" -> ci.code,
    "used" -> ci.usedBy.ids.length,
    "limitedNumber" -> ci.limitedNumber,
    "remaining" -> ci.remaining,
    "roles" -> ci.roles.map(_.toString())
  ).itself
  
  def toJsonFor(ci:CourseInvite, a:Approval[User]) = toJson(ci)
  
  def update(ci:CourseInvite, jsVal:JsValue) = {
    val rolechanges = (jsVal \ "roles").asOpt[Map[String, Boolean]].getOrElse(Map.empty)
    val add = for {
      (role, value) <- rolechanges if value
    } yield CourseRole.valueOf(role)
    val subtract = for {
      (role, value) <- rolechanges if !value
    } yield CourseRole.valueOf(role)

    ci.copy(
      code = (jsVal \ "code").asOpt[String].map(_.trim).filter(!_.isEmpty) getOrElse ci.code,
      limitedNumber = (jsVal \ "limitedNumber").asOpt[Boolean] getOrElse ci.limitedNumber,
      remaining = (jsVal \ "remaining").asOpt[Int] getOrElse ci.remaining,
      roles = ci.roles ++ add -- subtract
    )
  }  
}
  
  