package com.impressory.json

import com.impressory.api._
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import play.api.libs.json._
import com.wbillingsley.handyplay.JsonConverter

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

object CommentsToJson extends JsonConverter[Comments, User] {
  def toJsonFor(c:Comments, a: Approval[User]) = {
    for {
      ec <- (for {
        ec <- c.embedded.toRefMany
        j <- EmbeddedCommentToJson.toJsonFor(ec, a)
      } yield j).toRefOne
    } yield Json.obj(
      "count" -> c.count,
      "embedded" -> ec.toSeq
    )
  }

  def toJson(c:Comments) = toJsonFor(c, Approval(RefNone))
}