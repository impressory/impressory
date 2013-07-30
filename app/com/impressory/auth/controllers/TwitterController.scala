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

/**
 * Handles Twitter log-in. Based on sample code from Play Framework 
 * documentation.
 */
object TwitterController extends Controller {
  
  val KEY = ConsumerKey(
    // TODO: Fix this to throw a meaningful error message if the keys have not been set
    Play.configuration.getString("auth.twitter.ckey").getOrElse("No key"), 
    Play.configuration.getString("auth.twitter.csecret").getOrElse("No secret")
  )

  val TWITTER = OAuth(ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize", KEY),
    false)
    
  val TOKENNAME = "twittertoken"
  val SECRETNAME = "twittersecret"

  /**
   * Beginning of the Sign in with Twitter flow, using OAuth1.
   * 
   */
  def requestAuth = Action { implicit request =>      
    TWITTER.retrieveRequestToken(routes.TwitterController.callback.absoluteURL()) match {
      case Right(t) => {
        // We received the unauthorized tokens in the OAuth object - store it before we proceed
        Redirect(TWITTER.redirectUrl(t.token)).withSession(request.session + (TOKENNAME -> t.token) + (SECRETNAME -> t.secret))
      }
      case Left(e) => throw e
    }
  }  
  
  /**
   * Twitter redirects the user back to this action upon authorization
   */
  def callback = Action { implicit request =>
    
    /**
     * Finds a request or access token from the request, if there is one
     */
    def sessionTokenPair(implicit request: Request[_]): Ref[RequestToken] = {    
      Ref(for {
        token <- request.session.get(TOKENNAME)
        secret <- request.session.get(SECRETNAME)
      } yield {
        RequestToken(token, secret)
      })
    }    
    
    /**
     * Given an authentication token, goes and looks up that user's details on GitHub
     */
    def userFromAuth(token: RequestToken) = {
      val ws = WS.url("https://api.twitter.com/1.1/account/verify_credentials.json").sign(OAuthCalculator(KEY, token)).get()
      
      for (
        resp <- new RefFuture(ws);
        json = resp.json;
        id <- (json \ "id_str").asOpt[String]
      ) yield {
        InterstitialMemory(
            service = "twitter",
            id = id,
            name = (json \ "name").asOpt[String],
            nickname = (json \ "screen_name").asOpt[String],
            username = (json \ "screen_name").asOpt[String],
            avatar = (json \ "profile_image_url").asOpt[String]
          )
      }
    }      
    
    // Fetch the user data from Twitter
    val refMem = for (
      verifier <- Ref(request.getQueryString("oauth_verifier")) orIfNone Refused("Twitter did not provide a verification code");
      tokenPair <- sessionTokenPair(request);
      accessToken <- TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => t.itself
        case Left(e) => RefFailed(Refused("Twitter did not provide an access token"))
      };
      mem <- userFromAuth(accessToken) orIfNone Refused("Twitter did not provide any user data for that login")
    ) yield mem
    
    val res = for (
      mem <- refMem;
      user <- optionally(User.byIdentity("twitter", mem.id))
    ) yield {
      user match {
        case Some(u) => {
          val session = RequestUtils.withLoggedInUser(request.session, u.itself)
          Redirect(com.impressory.play.controllers.routes.Application.index).withSession(session)
        } 
        case None => {
          val session = request.session + (InterstitialController.sessionVar -> mem.toJsonString)
          Redirect(routes.InterstitialController.viewRegisterUser(Some("Twitter"))).withSession(session)
        }
      }
    }
    
    res
  }  

  /**
   * Calculates the Twitter ID from an access token
   */
  def idFromToken(r:RequestToken) = r.token.split("-")(0)

}