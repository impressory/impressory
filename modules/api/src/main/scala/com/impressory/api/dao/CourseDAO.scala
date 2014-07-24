package com.impressory.api.dao

import com.impressory.api.Course
import com.wbillingsley.handy.{RefFailed, Ref, LookUp}

trait CourseDAO {

  def lookUp:LookUp[Course,String]

  def saveNew(course:Course):Ref[Course]

  def saveExisting(course:Course):Ref[Course]

}

object NullCourseDAO extends CourseDAO {

  private val msg = "No CourseDAO has been configured"

  private def failed = RefFailed(new IllegalStateException(msg))

  def lookUp = LookUp.fails(msg)

  def saveNew(course:Course) = failed

  def saveExisting(course:Course) = failed
}