package com.impressory.play.controllers

import play.api.mvc.{Result, Request, RequestHeader, Action, AcceptExtractors, AnyContent}
import com.wbillingsley.handy._
import Ref._
import com.impressory.play.json.JsonConverters._

case class Angular[A](val action:Action[A]) extends Action[A] with AcceptExtractors {
  
  def apply(request: Request[A]): Result = {
    request match {
      case Accepts.Html() => ResultConversions.refResultToResult(Application.angularMain(request))(request)
      case Accepts.Json() => {
        try {
          action(request)      
        } catch {
          case exc:Throwable => ResultConversions.refResultToResult(RefFailed(exc))(request)
        }
      }
    }
  }

  lazy val parser = action.parser
  
}

