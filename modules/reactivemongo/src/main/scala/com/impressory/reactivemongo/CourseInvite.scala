package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._
import com.wbillingsley.handyplay.RefEnumIter
import com.impressory.api._

case class CourseInvite(
    
  var course:Ref[Course],

  var code:String = BSONObjectID.generate.stringify,

  var roles:Set[CourseRole] = Set(CourseRole.Reader),

  var addedBy:Ref[User] = RefNone,

  var limitedNumber:Boolean = false,

  var remaining:Int = 1,

  var _usedBy:Seq[BSONObjectID] = Seq.empty,
  
  var updated: Long = System.currentTimeMillis,

  val created: Long = System.currentTimeMillis,

  val _id: BSONObjectID = BSONObjectID.generate

) extends HasBSONId {
  
  def id = _id
  
  lazy val usedBy = new RefManyById(classOf[User], _usedBy)

}

object CourseInvite extends FindById[CourseInvite] {
  
  import Registration._
  
  val collName = "courseInvite"
    
  implicit val writer = Macros.writer[CourseInvite]
  
  implicit object bsonReader extends BSONDocumentReader[CourseInvite] {    
    def read(doc:BSONDocument):CourseInvite = {
      val ci = new CourseInvite(
          _id = doc.getAs[BSONObjectID]("_id").get,
          course = doc.getRef(classOf[Course], "course"),
          code = doc.getAs[String]("code").get,
          roles = doc.getAs[Set[CourseRole]]("roles").getOrElse(Set(CourseRole.Reader)),
          addedBy = doc.getRef(classOf[User], "addedBy"),
          limitedNumber = doc.getAs[Boolean]("limitedNumber").getOrElse(false),
          remaining = doc.getAs[Int]("remaining").getOrElse(1),
          _usedBy = doc.getAs[Seq[BSONObjectID]]("_usedBy").getOrElse(Seq.empty),
          updated = doc.getAs[Long]("updated").getOrElse(System.currentTimeMillis),
          created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis)
      )
      ci
    }    
  }  
  
  def use(invite:Ref[CourseInvite], user:Ref[User]) = {
    val query = BSONDocument("_id" -> invite)
    val update = BSONDocument(
      "$push" -> BSONDocument("_usedBy" -> user),
      "$inc" -> BSONDocument("remaining" -> -1)
    )
    val coll = DB.coll(collName)
    val fle = coll.update(query, update)
    val fut = fle.flatMap { _ => coll.find(query).one[CourseInvite] }
    new RefFutureOption(fut)    
  }
  
  def availableByCode(c:Ref[Course], code:String) = {
    val query = BSONDocument("course" -> c, "code" -> code, "$or" -> BSONArray(
        BSONDocument("limitedNumber" -> false),
        BSONDocument("remaining" -> BSONDocument("$gt" -> 0))
    ))
    val cursor = DB.coll(collName).find(query).one[CourseInvite]
    new RefFutureOption(cursor)
  }
  
  def byCourse(c:Ref[Course]) = {
    val query = BSONDocument("course" -> c)
    val cursor = DB.coll(collName).find(query).cursor[CourseInvite]
    new RefEnumIter(cursor.enumerateBulks)
  }
  
  def saveNew(ci:CourseInvite) = {
    val fle = DB.coll(collName).save(ci)
    val fut = fle.map { _ => ci.itself }
    new RefFutureRef(fut)    
  }
  
}