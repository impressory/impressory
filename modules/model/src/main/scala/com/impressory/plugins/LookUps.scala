package com.impressory.plugins

import com.wbillingsley.handy._
import Id._
import com.impressory.api._
import com.wbillingsley.handyplay.UserProvider

object LookUps {
  
  val catalog = new LookUpCatalog
  
  def lookUpFails[T] = new LookUp[T, Any] {
    def one[K <: Any](r:Id[T, K]) = RefFailed(new IllegalStateException("No look up method for this type has been configured"))

    def many[K <: Any](r:Ids[T, K]) = RefFailed(new IllegalStateException("No look up method for this type has been configured"))
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

  var registrationProvider = new RegistrationProvider {
    def find(user:Id[User, String], course:Id[Course, String]):Ref[Registration] = new RefFailed(new IllegalStateException("No registration provider has been configured."))
  }

  implicit def getUserProvider = userProvider

  var idAllocator:Option[Function0[String]] = None

  def allocateId[T]:Id[T,String] = idAllocator.get.apply.asId[T]

}
