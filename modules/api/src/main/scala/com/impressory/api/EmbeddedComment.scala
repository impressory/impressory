package com.impressory.api

import com.wbillingsley.handy._

case class EmbeddedComment (
  
  id:Id[EmbeddedComment,String],

  addedBy:Id[User,String],

  text:String = "",

  created:Long = System.currentTimeMillis,
  
  voting:UpDownVoting = new UpDownVoting
  
) extends HasStringId[EmbeddedComment]

case class Comments(
  count: Int = 0,

  embedded: Seq[EmbeddedComment] = Seq.empty
)