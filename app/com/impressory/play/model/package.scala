package com.impressory.play

/**
 * 
 */

import com.impressory.api._
import com.wbillingsley.handy.{Ref, LazyId}


package object model {
  
  /**
   * DataAction should retrieve user information using the UserDAO
   * from our database classes. (It includes methods for bySessionKey, etc) 
   */  
  implicit def userProvider = com.impressory.plugins.LookUps.userProvider

  import com.impressory.plugins.LookUps._

  def refUser(id:String) = LazyId(id).of[User]
  def refCourse(id:String) = LazyId(id).of[Course]
  def refContentEntry(id:String) = LazyId(id).of[ContentEntry]

}