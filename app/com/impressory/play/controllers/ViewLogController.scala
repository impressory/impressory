package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.play.model._
import ResultConversions._
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Enumeratee
import com.impressory.play.json.JsonConverters
import JsonConverters._
import com.impressory.reactivemongo.ViewLog

object ViewLogController extends Controller {
  
  val courseRegex = "/course/([^/?]+)".r
  val entryRegex = "/course/[^/?]+/entry/([^/?]+)".r
  
  /**
   * Updates the viewing statistics for a pageview
   */
  def addView = Action(parse.json) { implicit request => 
    
    val user = request.user
    val session = request.sessionKey
    val template = (request.body \ "template").asOpt[String] 
    val courseId = (request.body \ "params" \ "courseId").asOpt[String]
    val entryId = (request.body \ "params" \ "entryId").asOpt[String]
    val params = (request.body \ "params").as[Map[String, String]]
    
    val res = for (
      cId <- Ref(courseId);
      e <- optionally { for (eId <- Ref(entryId); e <- refContentEntry(eId)) yield e }; 
      record = ViewLog.Record(
        course = refCourse(cId),
        entry = e,
        user = user,
        session = session,
        template = template,
        params = params,
        how = Some("webapp")
      );
      fr <- ViewLog.log(record)
    ) yield NoContent  
    
    res 
    
  }

}