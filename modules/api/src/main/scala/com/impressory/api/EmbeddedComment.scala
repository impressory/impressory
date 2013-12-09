package com.impressory.api

import com.wbillingsley.handy.{Ref, RefNone, HasStringId}

case class EmbeddedComment (
  
  id:String,
    
  text:String = "",

  addedBy:Ref[User] = RefNone,
  
  created:Long = System.currentTimeMillis,
  
  voting:UpDownVoting = new UpDownVoting
  
) extends HasStringId