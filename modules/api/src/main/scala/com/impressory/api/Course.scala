package com.impressory.api

import com.wbillingsley.handy._
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

  addedBy:RefWithId[User] = RefNone,
    
  signupPolicy:CourseSignupPolicy = CourseSignupPolicy.open,

  chatPolicy:CourseChatPolicy = CourseChatPolicy.allReaders,
  
  listed:Boolean = false, 
  
  lti:LTIData = LTIData(),
  
  contentTags: CourseContentTags = CourseContentTags(),
  
  updated: Long = System.currentTimeMillis,
  
  created:Long = System.currentTimeMillis
  
) extends HasStringId


case class CourseContentTags(nouns: Set[String] = Set.empty, topics:Set[String] = Set.empty)

case class LTIData(key:String = Encrypt.genSaltB64, secret:String = Encrypt.genSaltB64)


case class Registration(

  course: RefWithId[Course],

  roles: Set[CourseRole] = Set(CourseRole.Reader),
  
  updated:Long = System.currentTimeMillis,

  created:Long = System.currentTimeMillis
  
) 
