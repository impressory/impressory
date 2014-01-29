package com.impressory

import _root_.reactivemongo.api._
import _root_.reactivemongo.bson._
import _root_.com.wbillingsley.handy._
import _root_.com.wbillingsley.handyplay._

import com.impressory.api._
import com.impressory.plugins.LookUps._
  
package object reactivemongo {
  
  
  implicit def refReader[T <: HasStringId](implicit lu:LookUp[T, String]) = new BSONReader[BSONObjectID, Ref[T]] {
    def read(id:BSONObjectID) = LazyId(id.stringify).of(lu)
  }
    
  implicit def refManyReader[T <: HasStringId](implicit lu:LookUp[T, String]) = new BSONReader[BSONArray, RefManyById[T, String]] {
    def read(ids:BSONArray) = {
      val arr = ids.as[Seq[BSONObjectID]].map(_.stringify)
      RefManyById(arr).of(lu)
    }
  }

  implicit class DocRefGetter(val doc: BSONDocument) extends AnyVal {
    def getRef[T](key:String)(implicit lu:LookUp[T, String]) = {
      val o:Option[BSONObjectID] = doc.getAs[BSONObjectID](key)
      o match {
        case Some(id) => LazyId(id.stringify).of(lu)
        case None => RefNone
      }
    }

    def getRefMany[T](key:String)(implicit lu:LookUp[T, String]):RefManyById[T, String] = {
      val arr = doc.getAs[Seq[BSONObjectID]](key).getOrElse(Seq.empty)
      val strings = arr.map(_.stringify)
      RefManyById(strings).of(lu)
    }
  }
  
}