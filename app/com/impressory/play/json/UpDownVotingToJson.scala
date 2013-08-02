package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._

object UpDownVotingToJson {

  def toJson(udv:UpDownVoting) = Json.obj("score" -> udv.score)
    
  def toJsonFor(udv:UpDownVoting, a: Approval[User]) = {
    a.who.getId match {
      case Some(id) => Json.obj(
        "score" -> udv.score,
        "voted" -> (udv._up.contains(id) || udv._down.contains(id))
      )
      case None => toJson(udv)
    }
  }  
  
}