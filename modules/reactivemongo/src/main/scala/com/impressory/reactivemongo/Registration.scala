package com.impressory.reactivemongo

import com.wbillingsley.handy._
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.concurrent.Execution.Implicits._

import com.impressory.api._

class Registration(

  val _course: BSONObjectID,

  var roles: Set[CourseRole] = Set(CourseRole.Reader),
  
  var updated:Long = System.currentTimeMillis,

  val created:Long = System.currentTimeMillis,

  val _id: BSONObjectID = BSONObjectID.generate
  
) extends HasBSONId {

  def id = _id
  
  def course = RefById(classOf[Course], _course)

}

object Registration {

  implicit object bsonCourseRoleWriter extends BSONWriter[CourseRole, BSONString] {
    def write(sr:CourseRole) = BSONString(sr.toString)
  }
  
  implicit object bsonCourseRoleReader extends BSONReader[BSONString, CourseRole] {
    def read(s:BSONString) = CourseRole.valueOf(s.value)
  }
  
  
  implicit object bsonWriter extends BSONDocumentWriter[Registration] {
    def write(reg: Registration) = BSONDocument(
      "_id" -> reg._id,
      "course" -> reg._course,
      "roles" -> reg.roles,
      "updated" -> reg.updated,
      "created" -> reg.created
    )
  }

  implicit object bsonReader extends BSONDocumentReader[Registration] {
    def read(doc: BSONDocument): Registration = {

      new Registration(
        _id = doc.getAs[BSONObjectID]("_id").get,
        _course = doc.getAs[BSONObjectID]("course").get,
        roles = doc.getAs[Set[CourseRole]]("roles").getOrElse(Set.empty),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
      )
    }
  }

}