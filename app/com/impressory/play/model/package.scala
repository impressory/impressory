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

  implicit def genLookUp[T, K] = com.impressory.plugins.LookUps.genLookUp[T, K]
    
  def refUser(id:String) = new LazyId(classOf[User], id)
  def refCourse(id:String) = new LazyId(classOf[Course], id)
  def refContentEntry(id:String) = new LazyId(classOf[ContentEntry], id)
  
}