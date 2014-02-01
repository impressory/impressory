package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.reactivemongo.ViewLog
import com.wbillingsley.handy.appbase.DataAction

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
    val courseId = (request.body \ "params" \ "courseId").asOpt[String]
    val entryId = (request.body \ "params" \ "entryId").asOpt[String]
    val params = (request.body \ "params").as[Map[String, String]]
    
    val res = for {
      userId <- request.user.refId
      cId <- Ref(courseId);
      e <- optionally { for (eId <- Ref(entryId); e <- refContentEntry(eId)) yield e }; 
      record = ViewLog.Record(
        course = refCourse(cId),
        entry = e,
        user = LazyId(userId).whichIs(request.user),
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