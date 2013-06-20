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
   * Action for users logging in with Twitter
   */
  def authenticate = Action { implicit request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          // This is where we log them into our app
          loginTwitterUser(t)(request)
        }
        case Left(e) => throw e
      }
    }.getOrElse(
      TWITTER.retrieveRequestToken(routes.TwitterController.authenticate.absoluteURL()) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Redirect(TWITTER.redirectUrl(t.token)).withSession(request.session + (TOKENNAME -> t.token) + (SECRETNAME -> t.secret))
        }
        case Left(e) => throw e
      })
  }

  /**
   * Action for users adding Twitter to an existing account
   */
  def addTwitter = Action { implicit request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          ResultConversions.refResultToResult(for (
            u <- request.user;
            u2 <- fillUserDetails(t, u)
          ) yield Redirect(com.impressory.play.controllers.routes.Application.viewSelf))
        }
        case Left(e) => throw e
      }
    }.getOrElse(
      TWITTER.retrieveRequestToken(routes.TwitterController.addTwitter.absoluteURL()) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Redirect(TWITTER.redirectUrl(t.token)).withSession(request.session + (TOKENNAME -> t.token) + (SECRETNAME -> t.secret))
        }
        case Left(e) => throw e
      })
  }

  /**
   * Calculates the Twitter ID from an access token
   */
  def idFromToken(r:RequestToken) = r.token.split("-")(0)

  /**
   * Finds a request or access token from the request, if there is one
   */
  def sessionTokenPair(implicit request: Request[_]): Option[RequestToken] = {    
    for {
      token <- request.session.get(TOKENNAME)
      secret <- request.session.get(SECRETNAME)
    } yield {
      RequestToken(token, secret)
    }
  }

  /**
   * Once we have a Twitter authenticated token, see if we can log this user in
   */
  def loginTwitterUser(token: RequestToken) = Action { implicit request =>
    val twitterId = idFromToken(token)
    val resp = (for (u <- User.byIdentity("twitter", twitterId)) yield {
      val session = RequestUtils.withLoggedInUser(request.session, u.itself)
      Redirect(com.impressory.play.controllers.routes.Application.index).withSession(session)
    }) orIfNone {
      val session = request.session + (TOKENNAME -> token.token) + (SECRETNAME -> token.secret)
      Redirect(routes.TwitterController.viewRegisterUser).withSession(session).itself
    }
    resp
  }
  
  /**
   * Interstitial saying this Twitter account hasn't been registered yet
   */
  def viewRegisterUser = Action { request => Ok(views.html.interstitials.registerTwitter())}
  
  /**
   * Register the Twitter user as a new user on this system
   */
  def registerUser = Action { implicit request => 
    
    val resp = for (
      token <- Ref(sessionTokenPair);
      user <- fillUserDetails(token, User.unsaved())
      
    ) yield {
      val session = RequestUtils.withLoggedInUser(request.session, user.itself)
      Redirect(com.impressory.play.controllers.routes.Application.index).withSession(session)
    }
    resp
  }

  def fillUserDetails(token: RequestToken, user: User) = {
    import play.api.libs.concurrent.Execution.Implicits._
    val ws = WS.url("https://api.twitter.com/1.1/account/verify_credentials.json").sign(OAuthCalculator(KEY, token))
    (for (
      response <- ws.get().toRef;
      json = response.json;
      a = { println(json); 1 };
      value <- (json \ "id_str").asOpt[String]
    ) yield {
      val i = Identity.unsaved(
        service = "twitter", value = value,
        avatar = (json \ "profile_image_url").asOpt[String])

      user.name = user.name orElse (json \ "name").asOpt[String]
      user.nickname = user.nickname orElse (json \ "screen_name").asOpt[String]
      if (user.identities.exists(i => i.service == "twitter" && i.value == value)) {
        user.itself
      } else {
        user.identities :+= i;
        User.save(user)
      }
    }).flatten    
  }
  

}