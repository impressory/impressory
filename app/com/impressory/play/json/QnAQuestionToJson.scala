package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._

import JsonConverters._

object QnAQuestionToJson {

  def toJsonFor(q:QnAQuestion, a: Approval[User]) = Json.obj(
    "id" -> q.id,
    "title" -> q.title,
    "text" -> q.text,
    "views" -> q.views,
    "voting" -> UpDownVotingToJson.toJsonFor(q.voting, a),
    "answerCount" -> q.answerCount,
    "answers" -> q.answers.map{ans => QnAAnswerToJson.toJsonFor(ans, a)},
    "commentCount" -> q.commentCount,
    "comments" -> q.comments.map {ec => EmbeddedCommentToJson.toJsonFor(ec, a)},
    "addedBy" -> q.addedBy,
    "created" -> q.created,
    "updated" -> q.updated
  )
  
}

object QnAAnswerToJson { 
  def toJsonFor(ans:QnAAnswer, a: Approval[User]) = Json.obj(
    "id" -> ans._id.stringify,
    "text" -> ans.text,
    "addedBy" -> ans.addedBy,
    "voting" -> UpDownVotingToJson.toJsonFor(ans.voting, a),
    "created" -> ans.created,
    "updated" -> ans.updated,
    "comments" -> ans.comments.map {ec => EmbeddedCommentToJson.toJsonFor(ec, a)}
  )
}