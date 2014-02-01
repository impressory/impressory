package com.impressory.api.enrol

import com.wbillingsley.handy._
import com.impressory.api._

case class CourseInvite(
    
  id: String,
    
  course:RefWithId[Course],

  code:String,

  roles:Set[CourseRole] = Set(CourseRole.Reader),

  addedBy:RefWithId[User] = RefNone,

  limitedNumber:Boolean = false,

  remaining:Int = 1,

  usedBy:RefManyById[User, String] =  RefManyById.empty,
  
  updated: Long = System.currentTimeMillis,

  created: Long = System.currentTimeMillis

) extends HasStringId