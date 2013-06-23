package com.impressory.reactivemongo

import com.wbillingsley.handy._
import com.wbillingsley.handyplay._
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.GetLastError

trait FindById[T] {
  
  val collName: String
  
  def coll = DB.coll(collName)
  
  implicit val bsonReader:BSONDocumentReader[T];

  def byId(id:BSONObjectID) = {    
    val fo = coll.find(BSONDocument("_id" -> id)).one[T]    
    new RefFutureOption(fo)
  } 
  
  def manyById(ids:Seq[BSONObjectID]) = {
    findMany(BSONDocument("_id" -> BSONDocument("$in" -> ids)))
  }
  
  def updateAndFetch(query:BSONDocument, update:BSONDocument):Ref[T] = {
    val c = coll
    val fle = c.update(query, update, GetLastError(true)) 
    val fut = fle.map { _ => new RefFutureOption(c.find(query).one[T]) } recover { case x:Throwable => RefFailed(x) }
    new RefFutureRef(fut)
  }
  
  def updateSafe(query:BSONDocument, update:BSONDocument, item:T):Ref[T] = {
    val c = coll
    val fle = c.update(query, update, GetLastError(true)) 
    val fut = fle.map { _ => RefItself(item) } recover { case x:Throwable => RefFailed(x) }
    new RefFutureRef(fut)
  }

  def updateUnsafe(query:BSONDocument, update:BSONDocument, item:T):Ref[T] = {
    val c = coll
    val fle = c.update(query, update, GetLastError(false)) 
    val fut = fle.map { _ => RefItself(item) } recover { case x:Throwable => RefFailed(x) }
    new RefFutureRef(fut)
  }
  
  def findMany(query:BSONDocument):RefMany[T] = {
    new RefEnumIter(coll.find(query).cursor[T].enumerateBulks)
  }
}