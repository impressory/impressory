package com.impressory.api.enrol

import com.wbillingsley.handy._
import com.impressory.api._

case class CourseInvite(
    
  id: Id[CourseInvite,String],
    
  course:Id[Course, String],

  code:String = scala.util.Random.alphanumeric.take(16).mkString,

  roles:Set[CourseRole] = Set(CourseRole.Reader),

  addedBy:Id[User,String],

  limitedNumber:Boolean = false,

  remaining:Int = 1,

  usedBy:Ids[User, String] = new Ids(Seq.empty),
  
  updated: Long = System.currentTimeMillis,

  created: Long = System.currentTimeMillis

) extends HasStringId[CourseInvite]