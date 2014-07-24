package com.impressory.api.dao

import com.impressory.api.{User, Course, Registration}
import com.wbillingsley.handy._

trait RegistrationDAO {

  def lookUp:LookUp[Registration,String]

  def save(r:Registration):Ref[Registration]

  def byUserAndCourse(u:Id[User,String], c:Id[Course,String]):Ref[Registration]

  def byUser(u:Id[User,String]):RefMany[Registration]


}

object NullRegistrationDAO extends RegistrationDAO {

  private val msg = "No RegistrationDAO has been configured"

  private def failed = RefFailed(new IllegalStateException(msg))

  def lookUp = LookUp.fails(msg)

  def save(r:Registration) = failed

  def byUserAndCourse(u:Id[User,String], c:Id[Course,String]) = failed

  def byUser(u:Id[User,String]) = failed


}