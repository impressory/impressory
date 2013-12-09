package com.impressory.play.eventroom

import play.api.libs.json.{Json, JsValue, Writes}
import com.wbillingsley.eventroom.{EventRoomGateway, Member, ListenTo, EREvent}
import com.wbillingsley.handy.Ref._
import com.wbillingsley.eventroom.Subscribe
import com.wbillingsley.eventroom.Unsubscribe

import com.impressory.api._

case class Mem(u: Option[User]) extends Member {
  
  val nickname = u.flatMap(_.nickname).getOrElse("Anonymous")
  
  implicit def toJson = Some(Json.obj(
    "id" -> u.map(_.id),
    "nickname" -> nickname 
  ))

}

object Mem {
  implicit object Writes extends Writes[Mem] {
    def writes(m:Mem) = m.toJson.get
  }
}

object EventRoom extends EventRoomGateway {
  import scala.language.implicitConversions
  
  implicit def uToM(u:Option[User]) = Mem(u)

}
