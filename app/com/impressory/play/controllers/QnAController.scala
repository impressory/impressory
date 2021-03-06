package com.impressory.play.controllers

import com.impressory.plugins.LookUps
import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.api.qna._
import play.api.libs.iteratee.Enumerator
import com.impressory.security.Permissions
import com.impressory.reactivemongo.ContentEntryDAO
import com.impressory.reactivemongo.QnAQuestionDAO

object QnAController extends Controller {

  import com.impressory.plugins.LookUps._
  import com.impressory.plugins.RouteConfig._
  
  implicit val cetoj = com.impressory.json.ContentEntryToJson
  
  /**
   * All the questions for a course
   */
  def listQuestions(rCourse:RefWithId[Course], skip:Option[Int] = None) = DataAction.returning.many { implicit request =>
      for (
          course <- rCourse;
          approved <- request.approval ask Permissions.readCourse(course.itself);
          question <- ContentEntryDAO.byKind(course.itself, QnAQuestion.itemType)
      ) yield question  
  }
  
  /**
   * Handle submission of the form to add an answer
   */
  def handleAddAnswer(rCourse:RefWithId[Course], rQ:RefWithId[ContentEntry]) = DataAction.returning.one(parse.json) { implicit request =>
      for {
        question <- rQ;
        user <- request.approval.who
        approved <- request.approval ask Permissions.readCourse(question.course);
        text <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("We need some text in that answer");
        updated <- QnAQuestionDAO.addAnswer(
          question.itself,
          new QnAAnswer(id=LookUps.allocateId, text=text, addedBy=user.id, session=Some(request.sessionKey))
        )
      } yield updated
  }
    
  /**
   * Handle submission of the form to add an answer
   *
  def handleAddAnswerComment(cid:String, qid:String, ansId:String) = DataAction.returning.one(parse.json) { implicit request =>
      val approval = request.approval
      for {
        question <- refContentEntry(qid);
        approved <- approval ask Permissions.Read(question.course)
        answer = RefById.of[QnAAnswer](ansId)
        text <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("We need some text in that comment")
        updated <- QnAQuestionDAO.addAnsComment(question.itself, answer, new EmbeddedComment(id=ContentEntryDAO.allocateId, text=text, addedBy=request.user))
      } yield updated
  }*/
  
}