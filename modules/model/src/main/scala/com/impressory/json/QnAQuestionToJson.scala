package com.impressory.json

import com.impressory.api._
import com.impressory.api.qna._

import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import play.api.libs.json._

object QnAQuestionToJson {

  def toJsonFor(q:QnAQuestion, appr: Approval[User]) = {
    for {
      ans <- {
        for {
          a <- q.answers.toRefMany
          j <- QnAAnswerToJson.toJsonFor(a, appr)
        } yield j
      }.toRefOne
    } yield Json.obj(
      "text" -> q.text,
      "answerCount" -> q.answerCount,
      "answers" -> ans.toSeq
    )
  }

}

object QnAAnswerToJson { 
  def toJsonFor(ans:QnAAnswer, a: Approval[User]) = {
    for {
      v <- UpDownVotingToJson.toJsonFor(ans.voting, a)
      c <- CommentsToJson.toJsonFor(ans.comments, a)
    } yield Json.obj(
      "id" -> ans.id,
      "text" -> ans.text,
      "addedBy" -> ans.addedBy,
      "voting" -> v,
      "created" -> ans.created,
      "updated" -> ans.updated,
      "comments" -> c
    )
  }
}