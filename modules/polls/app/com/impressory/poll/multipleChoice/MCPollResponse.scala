package com.impressory.poll.multipleChoice
import com.wbillingsley.handy._
import com.impressory.api._

case class PastResponse(
  answer: Set[Int],
  
  session: Option[String],
  
  updated: Long = System.currentTimeMillis
)

case class MCPollResponse (
  
  id: Id[MCPollResponse,String],
  
  poll: Id[ContentEntry, String],
  
  addedBy: Option[Id[User, String]],
  
  session: Option[String] = None,
  
  answer:Set[Int] = Set.empty,
  
  responses:Seq[PastResponse] = Seq.empty,
  
  updated: Long = System.currentTimeMillis

) extends HasStringId[MCPollResponse]
