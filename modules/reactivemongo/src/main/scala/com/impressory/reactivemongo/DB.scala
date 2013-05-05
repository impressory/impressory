package com.impressory.reactivemongo

import reactivemongo.api._
import indexes.{Index, IndexType}
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import com.wbillingsley.handy._
import reactivemongo.core.actors.Authenticate


object DB {
  
  var dbName = "impressory"
    
  var connectionString = "localhost:27017"
    
  var dbUser:Option[String] = None
  
  var dbPwd:Option[String] = None 
    
  lazy val driver = new MongoDriver
    
  lazy val connection = {
    val auth = for (u <- dbUser; p <- dbPwd) yield Authenticate(dbName, u, p)
    
    driver.connection(List(connectionString), auth.toSeq)    
  }
  
  lazy val db = connection.apply(dbName)
  
  def coll(name:String) = db(name)
  
  
}