package com.impressory.reactivemongo

import com.wbillingsley.handy.{HasStringId, Ref, RefManyById }

import reactivemongo.bson._
import reactivemongo.api._

object DBConnector extends com.wbillingsley.handy.reactivemongo.DBConnector {
  
  override var dbName = "impressory"
  
  /**
   * Implicit conversion that allows Ref[_] to be written as BSON
   */
  implicit def RefWriter[T <: HasStringId] = new BSONWriter[Ref[T], BSONValue] {
    def write(r:Ref[T]) = {
      if (useBSONIds) {
        r.getId.map(new BSONObjectID(_)).getOrElse(BSONNull)
      } else {
        r.getId.map(BSONString(_)).getOrElse(BSONNull)
      }
    }
  }  

  implicit def RefManyWriter[T <: HasStringId] = new BSONWriter[RefManyById[T, _], BSONValue] {
    def write(r:RefManyById[T,_]) = {
      if (useBSONIds) {
        BSONArray(r.getIds.map(new BSONObjectID(_)))
      } else {
        BSONArray(r.getIds.map(BSONString(_)))
      }
    }
  }  

  implicit def RefManyReader[T <: HasStringId](clazz: Class[T]) = new BSONReader[BSONArray, RefManyById[T, String]] {
    def read(a:BSONArray) = {
      val arr = a.as[Seq[BSONObjectID]]
      new RefManyById(clazz, arr.map(_.stringify))
    }
  }  
}