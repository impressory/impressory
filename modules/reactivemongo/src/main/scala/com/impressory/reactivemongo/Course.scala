package com.impressory.reactivemongo

import play.api.mvc.Action
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import com.wbillingsley.handy.{Id, RefFuture, Ref, RefWithId}
import com.wbillingsley.handy.reactivemongo.DAO
import com.wbillingsley.handyplay.{RefConversions, RefEnumerator, RefEnumIter}
import Ref._

import com.impressory.api._

import CommonFormats._

object CourseDAO extends DAO with com.impressory.api.dao.CourseDAO {
    
  type DataT = Course
  
  val collName = "course"
    
  val db = DBConnector
  
  val clazz = classOf[Course]

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  /** Converts PasswordLogin to and from BSON */
  implicit val ltiFormat = Macros.handler[LTIData]

  implicit val cctags = Macros.handler[CourseContentTags]

  implicit object bsonSignUpPolicyHandler extends BSONHandler[BSONString, CourseSignupPolicy] {
    def write(sr:CourseSignupPolicy) = BSONString(sr.toString)
    def read(s:BSONString) = CourseSignupPolicy.valueOf(s.value)
  }

  implicit object bsonChatPolicyHandler extends BSONHandler[BSONString, CourseChatPolicy] {
    def write(sr:CourseChatPolicy) = BSONString(sr.toString)
    def read(s:BSONString) = CourseChatPolicy.valueOf(s.value)
  }

  implicit val coverMatterFormat = Macros.handler[CoverMatter]

  implicit val settingsFormat = Macros.handler[CourseSettings]

  implicit object bsonWriter extends BSONDocumentWriter[Course] {
    def write(course:Course) = BSONDocument(
      "addedBy" -> course.addedBy,
      "coverMatter" -> course.coverMatter,
      "settings" -> course.settings,
      "contentTags" -> course.contentTags,
      "updated" -> System.currentTimeMillis
    )
    
    
    def writeNew(course:Course) = write(course) ++ BSONDocument(
      idIs(course.id),
      "created" -> System.currentTimeMillis
    )
  }

  implicit object bsonReader extends BSONDocumentReader[Course] {    
    def read(doc:BSONDocument):Course = {
      val course = new Course(
        id = doc.getAsTry[Id[Course, String]]("_id").get,
        addedBy = doc.getAsTry[Id[User, String]]("addedBy").get,
        coverMatter = doc.getAs[CoverMatter]("coverMatter").getOrElse(new CoverMatter),
        settings = doc.getAs[CourseSettings]("settings").getOrElse(new CourseSettings),
        contentTags = doc.getAs[CourseContentTags]("contentTags").getOrElse(CourseContentTags()),
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
    val query = BSONDocument("_id" -> course.getId)
    val update = BSONDocument("$addToSet" -> BSONDocument("contentTags.nouns" -> noun))
  }
  
  def pushTopic(course:RefWithId[Course], topic:String) = {
    val query = BSONDocument("_id" -> course.getId)
    val update = BSONDocument("$addToSet" -> BSONDocument("contentTags.topics" -> topic))
  }
}