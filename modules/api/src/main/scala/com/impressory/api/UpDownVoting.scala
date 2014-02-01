package com.impressory.api

import com.wbillingsley.handy._

case class UpDownVoting(
    
  up:RefManyById[User, String] = RefManyById.empty,
  
  down:RefManyById[User, String] = RefManyById.empty,
  
  score: Int = 0
    
) {

  def hasVoted(u:RefWithId[User]) = {
    u.getId match {
      case Some(id) => up.rawIds.contains(id) || down.rawIds.contains(id)
      case _ => false
    }
    
  }
}
