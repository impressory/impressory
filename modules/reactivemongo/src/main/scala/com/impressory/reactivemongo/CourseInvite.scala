package com.impressory.reactivemongo

import com.wbillingsley.handy.{RefFuture, Ref, RefNone, RefManyById, RefWithId}
import com.wbillingsley.handy.reactivemongo.DAO

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._

import com.impressory.api._
import com.impressory.api.enrol._


object CourseInviteDAO extends DAO {
  
  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  type DataT = CourseInvite
  
  val collName = "courseInvite"
    
  val db = DBConnector
  
  val clazz = classOf[CourseInvite]

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  def unsaved = CourseInvite(id=allocateId, course=RefNone, code=java.util.UUID.randomUUID().toString())
    
  import UserDAO.bsonCourseRoleWriter
  import UserDAO.bsonCourseRoleReader
  import DBConnector.RefManyWriter
    
  implicit val writer = Macros.writer[CourseInvite]
  
  implicit object bsonReader extends BSONDocumentReader[CourseInvite] {    
    def read(doc:BSONDocument):CourseInvite = {
      val ci = new CourseInvite(
          id = doc.getAs[BSONObjectID]("_id").get.stringify,
          course = doc.getRef[Course]("course"),
          code = doc.getAs[String]("code").get,
          roles = doc.getAs[Set[CourseRole]]("roles").getOrElse(Set(CourseRole.Reader)),
          addedBy = doc.getRef[User]("addedBy"),
          limitedNumber = doc.getAs[Boolean]("limitedNumber").getOrElse(false),
          remaining = doc.getAs[Int]("remaining").getOrElse(1),
          usedBy = doc.getRefMany[User]("usedBy"),
          updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
          created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
      )
      ci
    }    
  }  
  
  def use(invite:RefWithId[CourseInvite], user:RefWithId[User]) = {
    val query = BSONDocument("_id" -> invite)
    val update = BSONDocument(
      "$push" -> BSONDocument("usedBy" -> user),
      "$inc" -> BSONDocument("remaining" -> -1)
    )
    updateAndFetch(query, update)
  }
  
  def availableByCode(c:RefWithId[Course], code:String) = {
    val query = BSONDocument("course" -> c, "code" -> code, "$or" -> BSONArray(
        BSONDocument("limitedNumber" -> false),
        BSONDocument("remaining" -> BSONDocument("$gt" -> 0))
    ))
    findOne(query)
  }
  
  def byCourse(c:RefWithId[Course]) = {
    val query = BSONDocument("course" -> c)
    findMany(query)
  }
  
  def saveNew(ci:CourseInvite) = saveSafe(writer.write(ci), ci)
  
}