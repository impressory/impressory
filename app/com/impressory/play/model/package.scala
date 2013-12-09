package com.impressory.play

/**
 * 
 */

import com.impressory.api._
import com.wbillingsley.handy.{Ref, LazyId}


package object model {
    
  def refUser(id:String) = new LazyId(classOf[User], id)
  def refCourse(id:String) = new LazyId(classOf[Course], id)
  def refContentEntry(id:String) = new LazyId(classOf[ContentEntry], id)
  
}