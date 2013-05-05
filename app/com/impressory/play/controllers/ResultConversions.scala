package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import play.api._
import play.api.mvc._
import play.api.mvc.Results.{Ok, NotFound, Forbidden, Async, InternalServerError}
import scala.concurrent.promise
import play.api.libs.json._

object ResultConversions extends AcceptExtractors {
  
  import scala.language.implicitConversions

  /**
   * Converts a Ref[play.api.templates.Html] to a Result
   */
  implicit def refHtmlToResult(r:Ref[play.api.templates.Html]):play.api.mvc.AsyncResult = {
    Async {
      val p = promise[Result]
      r onComplete(
        onSuccess = p success Ok(_),
        onNone = p success NotFound(views.html.xErrorNotFound("Not found")),
        onFail = _ match {
          case Refused(msg) => p success Forbidden(views.html.xErrorForbidden(msg))
          case exc:Throwable => p success InternalServerError(views.html.xErrorInternalError(exc.getMessage))
        }
      )
      p.future
    }
  }
  
  /**
   * Converts a Ref[play.api.mvc.Result] to a Result
   */
  implicit def refResultToResult(r:Ref[Result])(implicit request:Request[_]) = {
    Async {
      val p = promise[Result]
      r onComplete(
        onSuccess = p success _,
        onNone = p success {
          request match {
            case Accepts.Html() => NotFound(views.html.xErrorNotFound("Not found"))
            case Accepts.Json() => NotFound(Json.obj("error" -> "not found"))
            case _ => NotFound
          }
        },
        onFail = _ match {
          case Refused(msg) => p success {
            request match {
              case Accepts.Html() => Forbidden(views.html.xErrorForbidden(msg))
              case Accepts.Json() => Forbidden(Json.obj("error" -> msg))
              case _ => Forbidden(msg)
            }            
          }
          case com.impressory.api.UserError(msg) => p success {
            request match {
              case Accepts.Json() => Ok(Json.obj("error" -> msg))
              case _ => InternalServerError("User error in non-JSON request: " + msg)
            }            
          }
          case exc:Throwable => p success {
            
            exc.printStackTrace()
            
            request match {
              case Accepts.Html() => InternalServerError(views.html.xErrorInternalError(exc.getMessage))
              case Accepts.Json() => InternalServerError(Json.obj("error" -> exc.getMessage))
              case _ => InternalServerError(exc.getMessage)
            }                        
          }
        }
      )
      p.future
    }
  }
  
}