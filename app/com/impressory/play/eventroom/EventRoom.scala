package com.impressory.play.eventroom

import com.wbillingsley.eventroom.EventRoomGateway
import com.impressory.api._
import com.impressory.api.events._

object EventRoom extends EventRoomGateway {
  import scala.language.implicitConversions
  
  implicit def uToM(u:Option[User]) = Mem(u)

}
