package com.impressory.api

import com.wbillingsley.handy._

case class Registration(

  id: Id[Registration, String],

  user: Id[User, String],

  course: Id[Course, String],

  roles: Set[CourseRole] = Set(CourseRole.Reader),

  updated:Long = System.currentTimeMillis,

  created:Long = System.currentTimeMillis

) extends HasStringId[Registration]

trait RegistrationProvider {
  def find(user:Id[User, String], course:Id[Course, String]):Ref[Registration]
}