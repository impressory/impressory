package com.impressory

import _root_.reactivemongo.api._
import _root_.reactivemongo.bson._
import _root_.com.wbillingsley.handy._
import _root_.com.wbillingsley.handyplay._

import com.impressory.api._

package object reactivemongo {
  
  def refReader[T <: HasStringId](c:Class[T]) = new BSONReader[BSONObjectID, Ref[T]] {
    def read(id:BSONObjectID) = new LazyId(c, id.stringify)
  }
    
  def refManyReader[T <: HasStringId](c:Class[T]) = new BSONReader[BSONArray, RefManyById[T, String]] {
    def read(ids:BSONArray) = {
      val arr = ids.as[Seq[BSONObjectID]].map(_.stringify)
      new RefManyById(c, arr)
    }
  }
  implicit val refCourseReader = refReader(classOf[Course])
  implicit val refManyCourseReader = refManyReader(classOf[Course])
  implicit val refUserReader = refReader(classOf[User])
  implicit val refContentEntryReader = refReader(classOf[ContentEntry])
  implicit val refManyUserReader = refManyReader(classOf[User])
  implicit val refManyEntryReader = refManyReader(classOf[ContentEntry])
  
  implicit class DocRefGetter(val doc: BSONDocument) extends AnyVal {
    def getRef[T](c:Class[T], key:String) = {
      val o:Option[BSONObjectID] = doc.getAs[BSONObjectID](key)
      o match {
        case Some(id) => new LazyId(c, id.stringify)
        case None => RefNone
      }
    }
  }
  
}