package com.impressory.api

import com.wbillingsley.handy._

case class EmbeddedComment (
  
  id:String,
    
  text:String = "",

  addedBy:RefWithId[User] = RefNone,
  
  created:Long = System.currentTimeMillis,
  
  voting:UpDownVoting = new UpDownVoting
  
) extends HasStringId