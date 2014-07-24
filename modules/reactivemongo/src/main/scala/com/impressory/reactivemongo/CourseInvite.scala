package com.impressory.reactivemongo

import com.wbillingsley.handy.{Id, Ids, RefFuture, Ref, RefNone, RefManyById, RefWithId}
import com.wbillingsley.handy.reactivemongo.DAO

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._

import com.impressory.api._
import com.impressory.api.enrol._


object CourseInviteDAO extends DAO with com.impressory.api.dao.CourseInviteDAO {
  
  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  type DataT = CourseInvite
  
  val collName = "courseInvite"
    
  val db = DBConnector
  
  val clazz = classOf[CourseInvite]

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  import UserDAO.bsonCourseRoleWriter
  import UserDAO.bsonCourseRoleReader
  import CommonFormats._
    
  implicit val writer = Macros.writer[CourseInvite]
  
  implicit object bsonReader extends BSONDocumentReader[CourseInvite] {    
    def read(doc:BSONDocument):CourseInvite = {
      val ci = new CourseInvite(
          id = doc.getAs[Id[CourseInvite, String]]("_id").get,
          course = doc.getAs[Id[Course,String]]("course").get,
          code = doc.getAs[String]("code").get,
          roles = doc.getAs[Set[CourseRole]]("roles").getOrElse(Set(CourseRole.Reader)),
          addedBy = doc.getAs[Id[User,String]]("addedBy").get,
          limitedNumber = doc.getAs[Boolean]("limitedNumber").getOrElse(false),
          remaining = doc.getAs[Int]("remaining").getOrElse(1),
          usedBy = doc.getAs[Ids[User,String]]("usedBy").getOrElse(new Ids(Seq.empty)),
          updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
          created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
      )
      ci
    }    
  }  
  
  def use(invite:RefWithId[CourseInvite], user:RefWithId[User]) = {
    val query = BSONDocument("_id" -> invite.getId)
    val update = BSONDocument(
      "$push" -> BSONDocument("usedBy" -> user.getId),
      "$inc" -> BSONDocument("remaining" -> -1)
    )
    updateAndFetch(query, update)
  }
  
  def availableByCode(c:RefWithId[Course], code:String) = {
    val query = BSONDocument("course" -> c.getId, "code" -> code, "$or" -> BSONArray(
        BSONDocument("limitedNumber" -> false),
        BSONDocument("remaining" -> BSONDocument("$gt" -> 0))
    ))
    findOne(query)
  }
  
  def byCourse(c:RefWithId[Course]) = {
    val query = BSONDocument("course" -> c.getId)
    findMany(query)
  }
  
  def saveNew(ci:CourseInvite) = saveSafe(writer.write(ci), ci)
  
}