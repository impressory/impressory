package com.impressory.api

import com.wbillingsley.handy._

case class Course (
  
  id:Id[Course,String],

  addedBy:Id[User,String],

  coverMatter: CoverMatter = new CoverMatter,

  settings: CourseSettings = new CourseSettings,

  contentTags: CourseContentTags = new CourseContentTags,
  
  updated: Long = System.currentTimeMillis,
  
  created:Long = System.currentTimeMillis
  
) extends HasStringId[Course]


case class CoverMatter(
  /** eg, Design Computing Studio 2 */
  title: Option[String] = None,

  /** eg, DECO2800 */
  shortName:Option[String] = None,

  shortDescription:Option[String] = None,

  /** If the home page for the course is hosted outside the system */
  website:Option[String] = None,

  /** The image to use for this course */
  coverImage:Option[String] = None
)

case class CourseSettings(
  signupPolicy:CourseSignupPolicy = CourseSignupPolicy.open,

  chatPolicy:CourseChatPolicy = CourseChatPolicy.allReaders,

  listed:Boolean = false,

  cors:String = "",

  lti:LTIData = new LTIData
)

case class CourseContentTags(nouns: Set[String] = Set.empty, topics:Set[String] = Set.empty)

case class LTIData(
  key:String = java.util.UUID.randomUUID().toString,

  secret:String = scala.util.Random.alphanumeric.take(16).mkString
)

object LTI



