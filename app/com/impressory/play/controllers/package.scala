package com.impressory.play

import play.api.mvc.Request

package object controllers {
  
  /**
   * DataAction should retrieve user information using the UserDAO
   * from our database classes. (It includes methods for bySessionKey, etc) 
   */  
  implicit val userProvider = com.impressory.reactivemongo.UserDAO

  def refUser(id:String) = com.impressory.play.model.refUser(id)
  def refCourse(id:String) = com.impressory.play.model.refCourse(id)
  def refContentEntry(id:String) = com.impressory.play.model.refContentEntry(id)
  
  
}