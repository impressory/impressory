package com.impressory.api

import com.wbillingsley.handy._

case class UpDownVoting(
    
  up:Ids[User, String] = new Ids(Seq.empty),
  
  down:Ids[User, String] = new Ids(Seq.empty),
  
  score: Int = 0
    
) {

  def hasVoted(u:RefWithId[User]) = {
    u.getId match {
      case Some(id) => up.ids.contains(id.id) || down.ids.contains(id.id)
      case _ => false
    }
    
  }
}
