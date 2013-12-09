package com.impressory.api

import com.wbillingsley.handy.{Ref, RefNone, RefManyById, HasStringId}

case class UpDownVoting(
    
  up:RefManyById[User, String] = new RefManyById(classOf[User], Seq.empty),
  
  down:RefManyById[User, String] = new RefManyById(classOf[User], Seq.empty),
  
  score: Int = 0
    
) {

  def hasVoted(u:Ref[User]) = {
    u.getId match {
      case Some(id) => up.rawIds.contains(id) || down.rawIds.contains(id)
      case _ => false
    }
    
  }
}
