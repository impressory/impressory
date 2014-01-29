package com.impressory.api.enrol

import com.wbillingsley.handy.{Ref, RefManyById, RefNone, HasStringId}
import com.impressory.api._

case class CourseInvite(
    
  id: String,
    
  course:Ref[Course],

  code:String,

  roles:Set[CourseRole] = Set(CourseRole.Reader),

  addedBy:Ref[User] = RefNone,

  limitedNumber:Boolean = false,

  remaining:Int = 1,

  usedBy:RefManyById[User, String] =  RefManyById.empty,
  
  updated: Long = System.currentTimeMillis,

  created: Long = System.currentTimeMillis

) extends HasStringId