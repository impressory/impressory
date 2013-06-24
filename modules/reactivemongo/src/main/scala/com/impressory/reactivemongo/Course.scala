package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import com.impressory.api.CourseChatPolicy
import com.impressory.api.CourseSignupPolicy
import com.impressory.api.UserError
import com.wbillingsley.handyplay.RefEnumIter

case class TitleAndTag(title:String, tag:String)

/**
 * Translated from "Book" in the previous version
 */
class Course (
  
  var title: Option[String] = None,

  var shortName:Option[String] = None,

  var shortDescription:Option[String] = None,

  var longDescription:Option[String] = None,

  var edition:Option[String] = None,

  var coverImageURL:Option[String] = None,

  var expired:Option[Long] = None,

  var signupPolicy:CourseSignupPolicy = CourseSignupPolicy.open,

  var chatPolicy:CourseChatPolicy = CourseChatPolicy.allReaders,
  
  var listNouns:Seq[TitleAndTag] = Seq(TitleAndTag("Lectures", "Lecture"), TitleAndTag("Tutorials", "Tutorial")),
  
  var listed:Boolean = false, 
  
  val created: Long = System.currentTimeMillis,
  
  val updated: Long = System.currentTimeMillis,
  
  val _id:BSONObjectID = BSONObjectID.generate
) extends HasBSONId {
  
  def id = _id
  
}

object Course extends FindById[Course] {
    
  val collName = "course"

  implicit object bsonSignUpPolicyWriter extends BSONWriter[CourseSignupPolicy, BSONString] {
    def write(sr:CourseSignupPolicy) = BSONString(sr.toString)
  }
  
  implicit object bsonSignUpPolicyReader extends BSONReader[BSONString, CourseSignupPolicy] {
    def read(s:BSONString) = CourseSignupPolicy.valueOf(s.value)
  }
  
  implicit object bsonChatPolicyWriter extends BSONWriter[CourseChatPolicy, BSONString] {
    def write(sr:CourseChatPolicy) = BSONString(sr.toString)
  }
  
  implicit object bsonChatPolicyReader extends BSONReader[BSONString, CourseChatPolicy] {
    def read(s:BSONString) = CourseChatPolicy.valueOf(s.value)
  }  

  implicit object bsonWriter extends BSONDocumentWriter[Course] {
    def write(course:Course) = BSONDocument(
      "title" -> course.title,
      "shortName" -> course.shortName,
      "shortDesc" -> course.shortDescription,
      "longDesc" -> course.longDescription,
      "edition" -> course.edition,
      "coverImage" -> course.coverImageURL,
      "expired" -> course.expired,
      "signUpPolicy" -> course.signupPolicy,
      "chatPolicy" -> course.chatPolicy,
      "listed" -> course.listed,
      "updated" -> System.currentTimeMillis
    )
    
    
    def writeNew(course:Course) = write(course) ++ BSONDocument(
      "_id" -> course._id,
      "created" -> System.currentTimeMillis
    )
  }

  implicit object bsonReader extends BSONDocumentReader[Course] {    
    def read(doc:BSONDocument):Course = {
      val course = new Course(
          _id = doc.getAs[BSONObjectID]("_id").get,
          title = doc.getAs[String]("title"),
          shortName = doc.getAs[String]("shortName"),
          shortDescription = doc.getAs[String]("shortDesc"),
          longDescription = doc.getAs[String]("longDesc"),
          edition = doc.getAs[String]("edition"),
          coverImageURL = doc.getAs[String]("coverImage"),
          expired = doc.getAs[Long]("expired"),
          signupPolicy = doc.getAs[CourseSignupPolicy]("signUpPolicy").getOrElse(CourseSignupPolicy.open),
          chatPolicy = doc.getAs[CourseChatPolicy]("chatPolicy").getOrElse(CourseChatPolicy.allReaders),
          listed = doc.getAs[Boolean]("listed").getOrElse(false),
          created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
          updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis)
      )
      course
    }    
  }  
  
  def listedCourses = {
    findMany(BSONDocument("listed" -> true))
  }  
  
  def saveNew(course:Course) = {
    saveSafe(bsonWriter.writeNew(course), course)
  }
  
  def saveExisting(course:Course) = {
    val query = BSONDocument("_id" -> course.id)
    val update = BSONDocument("$set" -> course)
    updateSafe(query, update, course)
  }
  
}