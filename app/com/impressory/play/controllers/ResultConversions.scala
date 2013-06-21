package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import play.api._
import play.api.mvc._
import play.api.mvc.Results.{Ok, NotFound, Forbidden, BadRequest, Async, InternalServerError}
import scala.concurrent.promise
import play.api.libs.json._
import com.impressory.play.model.JsonConverters._
import play.api.libs.iteratee.Enumerator

object ResultConversions extends AcceptExtractors {
  
  import scala.language.implicitConversions
  
  /**
   * Converts a Ref[obj] to an HTTP response, so long as there is a writer than can turn the object to a Ref[JsValue]
   */
  implicit def refOToResult[O : WritesRJ](r:Ref[O])(implicit request:Request[_]):play.api.mvc.AsyncResult = {
    val writesRJ = implicitly[WritesRJ[O]]
    refResultToResult( for (obj <- r; j <- writesRJ.writeRJ(obj)) yield Ok(j) )
  }
  
  /**
   * Converts a Ref[JsValue] to an HTTP response
   */
  implicit def refJToResult(r:Ref[JsValue])(implicit request:Request[_]):play.api.mvc.AsyncResult = {
    refResultToResult(for (j <- r) yield Ok(j))
  }

  /**
   * Streams a RefMany[JsValue] as an HTTP response. Note that errors will usually result in an empty stream.
   */
  implicit def refManyJToResult(r:RefMany[JsValue])(implicit request:Request[_]) = {
    import com.wbillingsley.handyplay.RefConversions._
    
    val en = Enumerator("[") andThen r.enumerate.stringify andThen Enumerator("]") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")
  }

  
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
              case Accepts.Json() => BadRequest(Json.obj("error" -> msg))
              case _ => BadRequest("User error in non-JSON request: " + msg)
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