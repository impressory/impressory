package com.impressory.reactivemongo

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import com.wbillingsley.handy.{Id, RefFuture, Ref, RefWithId, RefMany}
import com.wbillingsley.handy.reactivemongo.DAO
import com.wbillingsley.handyplay.RefEnumIter
import Ref._

import com.impressory.api._

import CommonFormats._

object RegistrationDAO extends DAO {

  type DataT = Registration

  val collName = "registration"

  val db = DBConnector

  val clazz = classOf[Registration]

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  implicit object courseRoleHandler extends BSONHandler[BSONString, CourseRole] {
    def write(sr:CourseRole) = BSONString(sr.toString)
    def read(s:BSONString) = CourseRole.valueOf(s.value)
  }

  implicit object bsonWriter extends BSONDocumentWriter[Registration] {
    def write(r:Registration) = BSONDocument(
      "_id" -> r.id,
      "user" -> r.user,
      "course" -> r.course,
      "roles" -> r.roles,
      "updated" -> System.currentTimeMillis,
      "created" -> r.created
    )
  }

  implicit object bsonReader extends BSONDocumentReader[Registration] {
    def read(doc:BSONDocument):Registration = {
      val r = new Registration(
        id = doc.getAs[Id[Registration, String]]("_id").get,
        user = doc.getAs[Id[User, String]]("user").get,
        course = doc.getAs[Id[Course, String]]("course").get,
        roles = doc.getAs[Set[CourseRole]]("roles").getOrElse(Set.empty),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis)
      )
      r
    }
  }

  def byUserAndCourse(u:Id[User,String], c:Id[Course,String]) = findOne(BSONDocument("user" -> u, "course" -> c))

  def byUser(u:Id[User,String]):RefMany[Registration] = findMany(BSONDocument("user" -> u))

  def save(r:Registration) = {
    saveSafe(bsonWriter.write(r), r)
  }


}
