package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.play.model._
import ResultConversions._
import JsonConverters._
import play.api.libs.iteratee.Enumerator

object QnAController extends Controller {

  /**
   * All the questions for a course
   */
  def listQuestions(cid:String, skip:Option[Int] = None) = Angular { Action { implicit request => 
    
      val approval = request.approval
      val questions = for (
          course <- refCourse(cid);
          approved <- approval ask Permissions.Read(course.itself);
          question <- QnAQuestion.byCourse(course.itself);
          json <- question.itself.toJsonFor(approval)
      ) yield json  
      
      val en = Enumerator("{ \"questions\": [") andThen questions.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
      Ok.stream(en).as("application/json")    
  }}
  
  /**
   * Views a Q&A Question
   */
  def question(cid:String, qid:String) = Angular { Action { implicit request => 
    val approval = request.approval
    val r = for (
      q <- refQnAQuestion(qid);
      approved <- approval ask Permissions.Read(q.course);
      json <- q.itself.toJsonFor(approval)
    ) yield Ok(json)
    r
  }}
  
  /**
   * Handle submission of the form to add a question to the book
   */
  def handleNewQuestion(cid:String) = Action(parse.json) { implicit request =>

      val approval = request.approval
      
      val resp = for (
        course <- refCourse(cid);
        approved <- approval ask Permissions.Read(course.itself);
        session = RequestUtils.sessionKey(request.session);
        title <- Ref((request.body \ "title").asOpt[String]) orIfNone UserError("We need a title for the question");
        body <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("We need a body for the question");
        saved <- QnAQuestion.saveNew(QnAQuestion(title, body, addedBy=approval.who, course=course.itself, session=session));
        json <- saved.itself.toJsonFor(approval)
      ) yield {
        Ok(Json.obj("question" -> json))
      }      
      resp
  }

  /**
   * Handle submission of the form to add an answer
   */
  def handleAddAnswer(cid:String, qid:String) = Action(parse.json) { implicit request =>
      
      val approval = request.approval
      val resp = for (
        question <- refQnAQuestion(qid);
        approved <- approval ask Permissions.Read(question.course);
        session = RequestUtils.sessionKey(request.session);
        text <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("We need some text in that answer");
        updated <- QnAQuestion.addAnswer(question.itself, new QnAAnswer(text=text, addedBy=approval.who, session=session));
        json <- updated.itself.toJsonFor(approval)
      ) yield {
        Ok(Json.obj("question" -> json))
      }      
      resp
  }
  
  /**
   * Handle submission of the form to add an answer
   */
  def handleAddQuestionComment(cid:String, qid:String) = Action(parse.json) { implicit request =>
      
      val approval = request.approval
      val resp = for (
        question <- refQnAQuestion(qid);
        approved <- approval ask Permissions.Read(question.course);
        text <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("We need some text in that comment");
        updated <- QnAQuestion.addQComment(question.itself, new EmbeddedComment(text=text, addedBy=approval.who));
        json <- updated.itself.toJsonFor(approval)
      ) yield {
        Ok(Json.obj("question" -> json))
      }      
      resp
  }
  
  /**
   * Handle submission of the form to add an answer
   */
  def handleAddAnswerComment(cid:String, qid:String, ansId:String) = Action(parse.json) { implicit request =>
      val approval = request.approval
      val resp = for (
        question <- refQnAQuestion(qid);
        approved <- approval ask Permissions.Read(question.course);
        answer = RefById(classOf[QnAAnswer], ansId);
        text <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("We need some text in that comment");
        updated <- QnAQuestion.addAnsComment(question.itself, answer, new EmbeddedComment(text=text, addedBy=approval.who));
        json <- updated.itself.toJsonFor(approval)        
      ) yield {
        Ok(Json.obj("question" -> json))
      }      
      resp
  }
  
}