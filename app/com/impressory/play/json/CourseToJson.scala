package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import RefMany._
import play.api.libs.json._
import JsonConverters._

object CourseToJson {

  /**
   * Raw JSON for a course
   */
  def toJson(course: Course) = {
    Json.obj(
      "id" -> course.id,
      "title" -> course.title,
      "shortName" -> course.shortName,
      "shortDescription" -> course.shortDescription,
      "longDescription" -> course.longDescription,
      "coverImageURL" -> course.coverImageURL,
      "listed" -> course.listed,
      "edition" -> course.edition)
  }

  /**
   * JSON for a Course, including registration and permission
   * information for this User.
   */
  def toJsonFor(course: Course, appr: Approval[User]) = {

    // Registrations. Note, can produce RefNone
    val reg = for (
      u <- appr.who;
      r <- Ref(u.registrations.find(_.course.getId == Some(course.id)));
      json <- r.itself.toJson
    ) yield json

    // Permissions.
    val perms = for (
      read <- optionally(appr ask Permissions.Read(course.itself));
      chat <- optionally(appr ask Permissions.Chat(course.itself));
      edit <- optionally(appr ask Permissions.EditCourse(course.itself));
      add <- optionally(appr ask Permissions.AddContent(course.itself))
    ) yield Json.obj(
      "read" -> read.isDefined,
      "add" -> add.isDefined,
      "chat" -> chat.isDefined,
      "edit" -> edit.isDefined)

    // Combine the JSON responses, noting that reg or perms might be RefNone
    for (
      r <- optionally(reg); p <- optionally(perms)
    ) yield toJson(course) ++ Json.obj(
      "registration" -> r,
      "permissions" -> p)

  }
}

object CourseInviteToJson {
  def toJson(ci: CourseInvite) = Json.obj(
    "code" -> ci.code,
    "used" -> ci._usedBy.length,
    "limitedNumber" -> ci.limitedNumber,
    "remaining" -> ci.remaining,
    "roles" -> ci.roles.map(_.toString())
  )
}
  
  