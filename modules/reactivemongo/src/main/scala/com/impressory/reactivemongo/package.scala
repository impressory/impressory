package com.impressory

import _root_.reactivemongo.api._
import _root_.reactivemongo.bson._
import _root_.com.wbillingsley.handy._

package object reactivemongo {
  
  implicit def RefWriter[T <: HasBSONId] = new BSONWriter[Ref[T], BSONValue] {
    def write(r:Ref[T]) = r.getId(HasBSONId.GetsBSONId).getOrElse(BSONNull)
  }
  
  
  implicit class DocRefGetter(val doc: BSONDocument) extends AnyVal {
    def getRef[T](c:Class[T], key:String) = {
      val o:Option[BSONObjectID] = doc.getAs[BSONObjectID](key)
      o match {
        case Some(id) => new LazyId(c, id)
        case None => RefNone
      }
    }
  }
  
  implicit object RefCourseReader extends BSONReader[BSONObjectID, Ref[Course]] {
    def read(id:BSONObjectID) = RefById(classOf[Course], id)
  }

  implicit object RefUserReader extends BSONReader[BSONObjectID, Ref[User]] {
    def read(id:BSONObjectID) = RefById(classOf[User], id)
  }
  
  
}