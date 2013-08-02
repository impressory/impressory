package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._

import JsonConverters._

object EmbeddedCommentToJson {
    def toJsonFor(ec:EmbeddedComment, a: Approval[User]) = Json.obj(
      "id" -> ec._id.stringify,
      "text" -> ec.text,
      "addedBy" -> ec.addedBy,
      "voting" -> UpDownVotingToJson.toJsonFor(ec.voting, a),
      "created" -> ec.created
    )
}