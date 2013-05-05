package com.impressory.play.eventroom

import play.api.libs.json.{Json, JsValue}
import com.wbillingsley.eventroom.{EventRoomGateway, Member, ListenTo, EREvent}
import com.wbillingsley.handy.Ref._
import com.wbillingsley.eventroom.Subscribe
import com.wbillingsley.eventroom.Unsubscribe

import com.impressory.play.model._

case class Mem(u: Option[User]) extends Member {
  
  val nickname = u.flatMap(_.nickname).getOrElse("Anonymous")
  
  def toJson = Some(Json.obj(
    "id" -> u.map(_.id.stringify),
    "nickname" -> nickname 
  ))

}

object EventRoom extends EventRoomGateway {
  import scala.language.implicitConversions
  
  implicit def uToM(u:Option[User]) = Mem(u)

}
