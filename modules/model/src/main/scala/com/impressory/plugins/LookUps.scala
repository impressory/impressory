package com.impressory.plugins

import com.wbillingsley.handy._
import Id._
import com.impressory.api._
import com.impressory.api.dao._
import com.wbillingsley.handyplay.UserProvider

object LookUps {
  
  var courseDAO:CourseDAO = NullCourseDAO
  var contentEntryDAO:ContentEntryDAO = NullContentEntryDAO
  var registrationDAO:RegistrationDAO = NullRegistrationDAO
  var courseInviteDAO:CourseInviteDAO = NullCourseInviteDAO
  var userDAO:UserDAO = NullUserDAO
  var chatCommentDAO:ChatCommentDAO = NullChatCommentDAO

  implicit def courseLookUp = courseDAO.lookUp
  implicit var userLookUp = userDAO.lookUp
  implicit var entryLookUp = contentEntryDAO.lookUp

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

  var idAllocator:Option[Function0[String]] = None

  def allocateId[T]:Id[T,String] = idAllocator match {
    case Some(ia) => ia.apply.asId[T]
    case _ => throw new IllegalStateException("No idAllocator has been configured in LookUps")
  }

}
