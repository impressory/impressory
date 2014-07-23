package com.impressory.api.events

import com.wbillingsley.eventroom._
import com.impressory.api._
import play.api.libs.json._
import com.wbillingsley.handyplay.JsonConverter
import com.wbillingsley.handy._
import Ref._

case class Mem(u: Option[User]) extends Member {
  
  val nickname = u.flatMap(_.nickname).getOrElse("Anonymous")
  
  implicit def toJson = Some(Json.obj(
    "id" -> u.map(_.id.id),
    "nickname" -> nickname 
  ))

}

object Mem {
  implicit object Writes extends Writes[Mem] {
    def writes(m:Mem) = m.toJson.get
  }
}

case class BroadcastStandard[T](courseId:Id[Course,String], cts:T)(implicit ttoj: JsonConverter[T, User]) extends BroadcastEvent(ChatStream(courseId)) {
  
  override def toJsonFor(mem:Member) = toJson
  
  override lazy val toJson = {
      ttoj.toJson(cts)
  }
}

case class BroadcastUnique[T](courseId:Id[Course,String], cts:T)(implicit ttoj: JsonConverter[T, User]) extends BroadcastEvent(ChatStream(courseId)) {
  override def toJsonFor(mem:Member) = mem match {
    case Mem(optUser) => ttoj.toJsonFor(cts, Approval(optUser.toRef))
    case _ => ttoj.toJson(cts)
  }
  
  override def toJson = {
      ttoj.toJson(cts)
  }
}