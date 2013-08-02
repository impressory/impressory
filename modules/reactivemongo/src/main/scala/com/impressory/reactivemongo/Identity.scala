package com.impressory.reactivemongo

import com.wbillingsley.handy._
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.concurrent.Execution.Implicits._
import com.impressory.api.CanSendToClient

class Identity(    
  
    val service:String,
    
    val value:String,
    
    val avatar:Option[String] = None,
    
    val username:Option[String] = None,
    
    val since:Long = System.currentTimeMillis,
  
    val _id:BSONObjectID = BSONObjectID.generate
    
) extends HasBSONId with CanSendToClient {
  
  def id = _id

}

object Identity {
      
  implicit object bsonWriter extends BSONDocumentWriter[Identity] {    
    def write(identity:Identity) = BSONDocument(
    	"_id" -> identity._id,
    	"username" -> identity.username,
    	"key" -> BSONDocument(
    	  "service" -> identity.service,
    	  "value" -> identity.value
    	),    	
    	"avatar" -> identity.avatar,
    	"since" -> identity.since
    )        
  }
  
  implicit object bsonReader extends BSONDocumentReader[Identity] {    
    def read(doc:BSONDocument):Identity = {      
      
      val key = doc.getAs[BSONDocument]("key").get
      new Identity(
          _id = doc.getAs[BSONObjectID]("_id").get,
          username = doc.getAs[String]("username"),
          service = key.getAs[String]("service").get,
          value = key.getAs[String]("value").get,
          since = doc.getAs[Long]("since").get,
          avatar = doc.getAs[String]("avatar")
      )
    }    
  }
  
  def unsaved(
      service:String, value:String,
      avatar:Option[String]=None, username:Option[String]=None
  ) = new Identity(service=service, value=value, avatar=avatar, username=username)
  
}