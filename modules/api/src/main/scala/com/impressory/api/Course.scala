package com.impressory.api

import com.wbillingsley.handy.{Ref, RefNone, HasStringId}
import com.wbillingsley.encrypt.Encrypt

case class Course (
  
  id:String,
    
  /** eg, Design Computing Studio 2 */
  title: Option[String] = None,
  
  /** eg, DECO2800 */
  shortName:Option[String] = None,
    
  shortDescription:Option[String] = None,
    
  /** If the home page for the course is hosted outside the system */
  website:Option[String] = None,
    
  /** The image to use for this course */
  coverImage:Option[String] = None,

  addedBy:Ref[User] = RefNone,
    
  signupPolicy:CourseSignupPolicy = CourseSignupPolicy.open,

  chatPolicy:CourseChatPolicy = CourseChatPolicy.allReaders,
  
  listed:Boolean = false, 
  
  lti:LTIData = LTIData(),
  
  updated: Long = System.currentTimeMillis,
  
  created:Long = System.currentTimeMillis
  
) extends HasStringId


case class LTIData(key:String = Encrypt.genSaltB64, secret:String = Encrypt.genSaltB64)


case class Registration(

  course: Ref[Course],

  roles: Set[CourseRole] = Set(CourseRole.Reader),
  
  updated:Long = System.currentTimeMillis,

  created:Long = System.currentTimeMillis
  
) 
