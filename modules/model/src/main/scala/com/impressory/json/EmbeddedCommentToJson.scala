package com.impressory.json

import com.impressory.api._
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import play.api.libs.json._
import com.wbillingsley.handy.appbase.JsonConverter

object EmbeddedCommentToJson extends JsonConverter[EmbeddedComment, User] {
    def toJsonFor(ec:EmbeddedComment, a: Approval[User]) = {
      for {
        udv <- UpDownVotingToJson.toJsonFor(ec.voting, a)
      } yield Json.obj(
        "id" -> ec.id,
        "text" -> ec.text,
        "addedBy" -> ec.addedBy,
        "voting" -> udv,
        "created" -> ec.created
      )
    }
    
    def toJson(ec:EmbeddedComment) = toJsonFor(ec, Approval(RefNone))
}