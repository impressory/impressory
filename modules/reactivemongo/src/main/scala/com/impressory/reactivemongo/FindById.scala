package com.impressory.reactivemongo

import com.wbillingsley.handy._
import com.wbillingsley.handyplay._
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.concurrent.Execution.Implicits._

trait FindById[T] {
  
  val collName: String
  
  implicit val bsonReader:BSONDocumentReader[T];

  def byId(id:BSONObjectID) = {    
    val query = BSONDocument("_id" -> id)
    val coll = DB.coll(collName)
    val fo = coll.find(query).one[T]    
    val rfr = new RefFutureRef(fo.map(Ref(_)))
    rfr
  } 
  
  def manyById(ids:Seq[BSONObjectID]) = {
    val query = BSONDocument("_id" -> BSONDocument("$in" -> ids))
    val coll = DB.coll(collName)
    val fc = coll.find(query).cursor[T]
    val rfi = new RefEnumIter(fc.enumerateBulks)
    rfi
  }
  
}