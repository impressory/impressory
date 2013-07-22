package com.impressory.auth.controllers

import play.api.libs.oauth.{OAuth, OAuthCalculator, ConsumerKey, ServiceInfo, RequestToken}
import play.api.mvc.{Controller, Action, Request}
import play.api.libs.ws.WS
import com.wbillingsley.handy._
import Ref._
import play.api.Play
import Play.current
import com.impressory.play.model._
import com.impressory.play.controllers.{RequestUtils, RequestUser, ResultConversions}
import com.impressory.play.controllers.ResultConversions._
import play.api.libs.ws.WS.WSRequest
import play.api.libs.oauth.OAuthCalculator


object LTIAuthController extends Controller {
  
  /**
   * Logs a user in 
   */
  def ltiLaunch(courseId:String) = Action { implicit request =>
    
    println(request.body.asFormUrlEncoded)
    
    val resp = (for (
      course <- refCourse(courseId);
      valid <- validateOAuthSignature(request, course.lti.key, course.lti.secret);
      params <- Ref(request.body.asFormUrlEncoded);
      tool_consumer_instance_guid <- params.get("tool_consumer_instance_guid").flatMap(_.headOption) orIfNone RefFailed(Refused("The LTI data from your provider did not include a user id"));
      user_id <- params.get("user_id").flatMap(_.headOption) orIfNone RefFailed(Refused("The LTI data from your provider did not include a user id"))      
    ) yield {
      
      (for (user <- User.byIdentity(tool_consumer_instance_guid, user_id)) yield {
        val session = RequestUtils.withLoggedInUser(request.session, user.itself)
        Redirect(com.impressory.play.controllers.routes.CourseController.get(courseId)).withSession(session)
      }) orIfNone {
        var session = request.session + ("lti_user_id" -> user_id) + 
          ("tool_consumer_instance_guid" -> tool_consumer_instance_guid)
        
        for (v <- params.get("lis_person_name_given").flatMap(_.headOption)) session += ("lis_person_name_given" -> v)
        for (v <- params.get("lis_person_name_full").flatMap(_.headOption)) session += ("lis_person_name_full" -> v)
        
        Redirect(routes.LTIAuthController.viewRegisterUser(courseId)).withSession(session).itself
      }
    }).flatten
    
    resp
  }
  
  /**
   * Interstitial saying this Twitter account hasn't been registered yet
   */
  def viewRegisterUser(courseId:String) = Action { request => Ok(views.html.interstitials.registerLTI())}  

  /**
   * Register the LTI user as a new user on this system
   */
  def registerNewUser(courseId:String) = Action { implicit request => 
    
    println("called!")
    val resp = (for (
      ltiUserId <- Ref(request.session.get("lti_user_id"));
      tool_consumer_instance_guid <- request.session.get("tool_consumer_instance_guid")
    ) yield {
      val identity = Identity.unsaved(service=tool_consumer_instance_guid, value=ltiUserId)
      val user = User.unsaved(name=request.session.get("lis_person_name_full"), nickname=request.session.get("lis_person_name_given"))
      user.identities :+= identity
      val session = RequestUtils.withLoggedInUser(request.session, User.save(user))
      Redirect(com.impressory.play.controllers.routes.CourseController.get(courseId)).withSession(session)
    })
    resp
  }
  
  def validateOAuthSignature(request:Request[_], key:String, secret:String):Ref[String] = {
    
    /*
     * TODO: Implement this! 
     */
    
    "not implemented yet".itself  // Very temporary hack -- normally this should be a RefFailed if not implemented!
  }

}