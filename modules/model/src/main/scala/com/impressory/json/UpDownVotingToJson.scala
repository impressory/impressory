package com.impressory.json

import com.impressory.api._
import com.wbillingsley.handy._
import com.wbillingsley.handy.appbase.JsonConverter
import com.wbillingsley.handy.Ref._
import play.api.libs.json._

object UpDownVotingToJson extends JsonConverter[UpDownVoting, User]{

  def toJson(udv:UpDownVoting) = Json.obj("score" -> udv.score).itself
    
  def toJsonFor(udv:UpDownVoting, a: Approval[User]) = {
    (for {
      id <- a.who.refId
    } yield Json.obj(
      "score" -> udv.score,
      "voted" -> (udv.up.rawIds.contains(id) || udv.down.rawIds.contains(id))
    )) orIfNone toJson(udv)
  }
  
}