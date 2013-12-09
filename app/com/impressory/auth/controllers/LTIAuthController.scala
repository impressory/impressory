package com.impressory.auth.controllers

import play.api.libs.oauth.{OAuth, OAuthCalculator, ConsumerKey, ServiceInfo, RequestToken}
import play.api.mvc.{Controller, Action, Request}
import play.api.libs.ws.WS
import com.wbillingsley.handy._
import Ref._
import play.api.Play
import Play.current
import com.impressory.api._
import com.impressory.play.model.refCourse
import play.api.libs.ws.WS.WSRequest
import play.api.libs.oauth.OAuthCalculator
import com.impressory.reactivemongo.UserDAO
import com.wbillingsley.handy.appbase.DataAction
import com.wbillingsley.handy.playoauth.OAuthDetails
import com.wbillingsley.handy.playoauth.UserRecord
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import com.wbillingsley.handy.playoauth.PlayAuth
import scala.util.Success
import com.wbillingsley.handy.appbase.DataAction.BodyAction
import play.api.mvc.BodyParsers
import play.api.mvc.EssentialAction


object LTIAuthController extends Controller {
  
  import com.impressory.play.controllers.userProvider
  
  /**
   * Logs a user in 
   */
  def ltiLaunch(courseId:String) = EssentialAction { request =>
    ltiBodyAction(courseId).apply(request)
  }
    
  def ltiBodyAction(courseId:String) = new BodyAction(BodyParsers.parse.anyContent)({ implicit request =>
    println(request.body.asFormUrlEncoded)
    
    val resp = for {
      course <- refCourse(courseId);
      valid <- validateOAuthSignature(request, course.lti.key, course.lti.secret);
      params <- Ref(request.body.asFormUrlEncoded);
      tool_consumer_instance_guid <- params.get("tool_consumer_instance_guid").flatMap(_.headOption) orIfNone RefFailed(Refused("The LTI data from your provider did not include a user id"));
      user_id <- params.get("user_id").flatMap(_.headOption) orIfNone RefFailed(Refused("The LTI data from your provider did not include a user id"))
      oauthDetails = OAuthDetails(
        userRecord = UserRecord(
            service=tool_consumer_instance_guid, 
            id=user_id,
            name=None,
            username=None,
            nickname=None,
            avatar=None
        ),
        raw = Some(Json.obj(
          "tool_consumer_instance_guid" -> tool_consumer_instance_guid,
          "user_id" -> user_id
        ))
      )
      act = PlayAuth.onAuth(Success(oauthDetails))
      
    } yield act(request)
      
    DataAction.refEE(resp)
  })
  
  
  def validateOAuthSignature(request:Request[_], key:String, secret:String):Ref[String] = {
    
    /*
     * TODO: Implement this! 
     */
    
    "not implemented yet".itself  // Very temporary hack -- normally this should be a RefFailed if not implemented!
  }

}