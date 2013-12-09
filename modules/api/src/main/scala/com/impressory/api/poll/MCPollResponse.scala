package com.impressory.api.poll

import com.wbillingsley.handy.{Ref, RefNone, HasStringId}
import com.impressory.api._

case class MCPollResponse (
  
  id: String,
  
  poll: Ref[ContentEntry] = RefNone,
  
  addedBy: Ref[User] = RefNone,
  
  session: Option[String] = None,
  
  answer:Set[Int] = Set.empty,
  
  updated: Long = System.currentTimeMillis

) extends HasStringId
