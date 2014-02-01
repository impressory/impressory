package com.impressory.reactivemongo

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import com.wbillingsley.handy.{RefFuture, Ref, RefWithId}
import com.wbillingsley.handy.reactivemongo.DAO
import com.wbillingsley.handyplay.RefEnumIter
import Ref._

import com.impressory.api._
import com.wbillingsley.encrypt.Encrypt

object CourseDAO extends DAO {
    
  type DataT = Course
  
  val collName = "course"
    
  val db = DBConnector
  
  val clazz = classOf[Course]

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  def unsaved = Course(id=allocateId)
    

  /** Converts PasswordLogin to and from BSON */
  implicit val ltiFormat = Macros.handler[LTIData]
    
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
      "coverImage" -> course.coverImage,
      "signUpPolicy" -> course.signupPolicy,
      "chatPolicy" -> course.chatPolicy,
      "listed" -> course.listed,
      "lti" -> course.lti,
      "updated" -> System.currentTimeMillis
    )
    
    
    def writeNew(course:Course) = write(course) ++ BSONDocument(
      "_id" -> BSONObjectID(course.id),
      "created" -> System.currentTimeMillis
    )
  }

  implicit object bsonReader extends BSONDocumentReader[Course] {    
    def read(doc:BSONDocument):Course = {
      val course = new Course(
          id = doc.getAs[BSONObjectID]("_id").get.stringify,
          title = doc.getAs[String]("title"),
          shortName = doc.getAs[String]("shortName"),
          shortDescription = doc.getAs[String]("shortDesc"),
          coverImage = doc.getAs[String]("coverImage"),
          signupPolicy = doc.getAs[CourseSignupPolicy]("signUpPolicy").getOrElse(CourseSignupPolicy.open),
          chatPolicy = doc.getAs[CourseChatPolicy]("chatPolicy").getOrElse(CourseChatPolicy.allReaders),
          listed = doc.getAs[Boolean]("listed").getOrElse(false),
          lti = doc.getAs[LTIData]("lti").getOrElse(LTIData()),
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
  
  def pushNoun(course:RefWithId[Course], noun:String) = {
    val query = BSONDocument("_id" -> course)
    val update = BSONDocument("$addToSet" -> BSONDocument("contentTags.nouns" -> noun))
  }
  
  def pushTopic(course:RefWithId[Course], topic:String) = {
    val query = BSONDocument("_id" -> course)
    val update = BSONDocument("$addToSet" -> BSONDocument("contentTags.topics" -> topic))
  }
}