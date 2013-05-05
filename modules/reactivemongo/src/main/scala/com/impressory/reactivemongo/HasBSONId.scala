package com.impressory.reactivemongo

import scala.util.Try
import com.wbillingsley.handy.{HasId, GetsId}
import reactivemongo.bson.BSONObjectID

trait HasBSONId extends HasId[BSONObjectID] {
  
}

object HasBSONId {
  
  implicit object GetsBSONId extends GetsId[HasBSONId, BSONObjectID] {
    
	def getId(obj:HasBSONId) = Some(obj.id)
  
	def canonical(key:Any) = {
	  key match {
	    case b:BSONObjectID => Some(b)
	    case _ => Try(new BSONObjectID(key.toString)).toOption
	  } 
	}    
  }   
}