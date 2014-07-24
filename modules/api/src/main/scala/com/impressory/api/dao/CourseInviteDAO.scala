package com.impressory.api.dao

import com.impressory.api.{Course, User}
import com.impressory.api.enrol.CourseInvite
import com.wbillingsley.handy._

trait CourseInviteDAO {

  def lookUp:LookUp[CourseInvite,String]

  def use(invite:RefWithId[CourseInvite], user:RefWithId[User]):Ref[CourseInvite]

  def availableByCode(c:RefWithId[Course], code:String):Ref[CourseInvite]

  def byCourse(c:RefWithId[Course]):RefMany[CourseInvite]

  def saveNew(ci:CourseInvite):Ref[CourseInvite]

}

object NullCourseInviteDAO extends CourseInviteDAO {

  private val msg = "No CourseInviteDAO has been configured"

  private def failed = RefFailed(new IllegalStateException(msg))

  def lookUp = LookUp.fails(msg)

  def use(invite:RefWithId[CourseInvite], user:RefWithId[User]) = failed

  def availableByCode(c:RefWithId[Course], code:String) = failed

  def byCourse(c:RefWithId[Course]) = failed

  def saveNew(r:CourseInvite) = failed

}