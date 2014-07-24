package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import Id._
import com.wbillingsley.handyplay._
import RefConversions._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.json._
import com.impressory.reactivemongo.ViewLog
import com.impressory.plugins.LookUps._

object ViewLogController extends Controller {
  
  import com.impressory.plugins.LookUps._
  import com.impressory.plugins.RouteConfig._
  
  val courseRegex = "/course/([^/?]+)".r
  val entryRegex = "/course/[^/?]+/entry/([^/?]+)".r
  
  /**
   * Updates the viewing statistics for a pageview
   */
  def addView = DataAction.returning.result(parse.json) { implicit request => 
    
    val session = request.sessionKey
    val template = (request.body \ "template").asOpt[String] 
    val courseId = (request.body \ "params" \ "courseId").asOpt[Id[Course, String]]
    val entryId = (request.body \ "params" \ "entryId").asOpt[Id[ContentEntry,String]]
    val params = (request.body \ "params").as[Map[String, String]]
    
    val res = for {
      userId <- request.user.refId
      cId <- Ref(courseId);
      e <- optionally { for (e <- entryId.lazily) yield e };
      record = ViewLog.Record(
        course = cId,
        entry = e,
        user = userId,
        session = Some(request.sessionKey),
        template = template,
        params = params,
        how = Some("webapp")
      );
      fr <- ViewLog.log(record)
    } yield NoContent
    
    res 
    
  }

}