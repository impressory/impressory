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
import com.wbillingsley.handyplay.DataAction
import DataAction.BodyAction
import com.wbillingsley.handy.playoauth.OAuthDetails
import com.wbillingsley.handy.playoauth.UserRecord
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import com.wbillingsley.handy.playoauth.PlayAuth
import scala.util.Success
import play.api.mvc.BodyParsers
import play.api.mvc.EssentialAction


object LTIAuthController extends Controller {

  // Import LookUp configuration
  import com.impressory.plugins.LookUps._
  
  /**
   * Logs a user in 
   */
  def ltiLaunch(courseId:String) = EssentialAction { request =>
    ltiBodyAction(courseId).apply(request)
  }
    
  def ltiBodyAction(courseId:String) = new BodyAction(BodyParsers.parse.anyContent)({ implicit request =>

    def getParam(params:Map[String, Seq[String]], name:String) = params.get(name).flatMap(_.headOption)


    
    val resp = for {
      course <- refCourse(courseId) orIfNone Refused("No such course")
      valid <- validateOAuthSignature(request, course.settings.lti.key, course.settings.lti.secret);
      params <- Ref(request.body.asFormUrlEncoded) orIfNone Refused("OAuth response had no parameters")
      tool_consumer_instance_guid <- {
        getParam(params, "oauth_consumer_key") orElse
        getParam(params, "tool_consumer_instance_guid") orIfNone Refused("The LTI data from your provider did not include a user id")
      }
      user_id <- params.get("user_id").flatMap(_.headOption) orIfNone Refused("The LTI data from your provider did not include a user id")
      oauthDetails = OAuthDetails(
        userRecord = UserRecord(
            service=tool_consumer_instance_guid,
            id=user_id,
            name=getParam(params, "lis_person_name_full"),
            username=Some(user_id),
            nickname=getParam(params, "lis_person_name_full") orElse Some(user_id),
            avatar=getParam(params, "user_image")
        ),
        raw = Some(Json.obj(
          "tool_consumer_instance_guid" -> tool_consumer_instance_guid,
          "user_id" -> user_id
        ))
      )
      act = PlayAuth.onAuth(Success(oauthDetails))
      
    } yield act(request)
    
    import play.api.libs.iteratee._
    import play.api.mvc._
    import play.api.libs.concurrent.Execution.Implicits._
    
    val handled:Ref[Iteratee[Array[Byte], SimpleResult]] = resp recoverWith {
      case x:Throwable => Done(Results.Forbidden(x.getMessage), Input.Empty).itself
    }
    
    Iteratee.flatten(handled.toFutOpt.map(_.getOrElse(Done(Results.Forbidden("Computer says no"), Input.Empty))))
  })
  
  
  def validateOAuthSignature(request:Request[_], key:String, secret:String):Ref[String] = {
    
    /*
     * TODO: Implement this! 
     */
    
    "not implemented yet".itself  // Very temporary hack -- normally this should be a RefFailed if not implemented!
  }

}