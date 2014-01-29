package com.impressory.plugins

import com.wbillingsley.handy._
import com.impressory.api._
import com.wbillingsley.handy.appbase.UserProvider

object LookUps {
  
  val catalog = new LookUpCatalog
  
  implicit def genLookUp[T,K](clazz:Class[T]) = catalog.genLookUp[T, K](clazz)

  def lookUpFails[T] = new LookUp[T, Any] {
    def lookUpOne[K <: Any](r:RefById[T, K]) = RefFailed(new IllegalStateException("No look up method for this type has been configured"))

    def lookUpMany[K <: Any](r:RefManyById[T, K]) = RefFailed(new IllegalStateException("No look up method for this type has been configured"))
  }

  implicit var courseLookUp:LookUp[Course,String] = lookUpFails[Course]
  implicit var userLookUp:LookUp[User,String] = lookUpFails[User]
  implicit var entryLookUp:LookUp[ContentEntry,String] = lookUpFails[ContentEntry]


  /**
   * How the application figures out which user is making a request
   */
  var userProvider:UserProvider[User] = new UserProvider[User] {
    
    def failure = throw new IllegalStateException(s"No user provider has been configured. This class is ${this.getClass.getName} configured is ${LookUps.userProvider.getClass.getName}")
    
    def byEmailAndPassword(email:String, password:String) = failure
    
    def byUsernameAndPassword(u:String, password:String) = failure
    
    def byIdentity(service:String, id:String) = failure
    
    def bySessionKey(id:String) = failure
  }
  
  implicit def getUserProvider = userProvider

}
