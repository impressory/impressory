package com.impressory.json

import com.impressory.api._
import com.impressory.api.enrol._
import com.impressory.security._

import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.RefMany._
import com.wbillingsley.handy.appbase.JsonConverter
import play.api.libs.json._


object CourseToJson extends JsonConverter[Course, User]{
  
  implicit val cctToJson = Json.writes[CourseContentTags]

  /**
   * Raw JSON for a course
   */
  def toJson(course: Course) = {
    Json.obj(
      "id" -> course.id,
      "title" -> course.title,
      "shortName" -> course.shortName,
      "shortDescription" -> course.shortDescription,
      "coverImage" -> course.coverImage,
      "contentTags" -> course.contentTags,
      "listed" -> course.listed
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
      r <- u.registrations.find(_.course.getId == Some(course.id)).toRef
      j <- RegistrationToJson.toJson(r)
    } yield j

    // Permissions.
    val perms = for {
      read <- appr askBoolean Permissions.Read(course.itself)
      chat <- appr askBoolean Permissions.Chat(course.itself)
      edit <- appr askBoolean Permissions.EditCourse(course.itself)
      add <- appr askBoolean Permissions.AddContent(course.itself)
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
      title = (jsVal \ "title").asOpt[String] orElse course.title,
      shortName = (jsVal \ "shortName").asOpt[String] orElse course.shortName,
      shortDescription = (jsVal \ "shortDescription").asOpt[String] orElse course.shortDescription,
      coverImage = (jsVal \ "coverImage").asOpt[String] orElse course.coverImage,
      listed = (jsVal \ "listed").asOpt[Boolean] getOrElse course.listed
    )
  }    
}

object CourseInviteToJson extends JsonConverter[CourseInvite, User] {
  
  def toJson(ci: CourseInvite) = Json.obj(
    "code" -> ci.code,
    "used" -> ci.usedBy.rawIds.length,
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
  
  