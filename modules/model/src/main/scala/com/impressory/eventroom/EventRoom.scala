package com.impressory.eventroom

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.eventroom._
import com.impressory.api._
import com.impressory.json._
import com.impressory.security.Permissions
import com.impressory.api.events._
import scala.language.implicitConversions

import play.api.libs.json._

object EventRoom extends EventRoomGateway {
  import scala.language.implicitConversions
  
  implicit def uToM(u:Option[User]) = Mem(u)

  var ltHandlers:Seq[ListenToJsonHandler] = Seq(ChatStreamLTJH)
  
  /**
   * Registers a handler that can read this ListenTo from JSON,
   * enabling the event room controller to process subscribe and unsubscribe messages
   */
  def registerListenTo(lh:ListenToJsonHandler) = this.synchronized {
    ltHandlers :+= lh
  }
  
  def listenToFromJson(j:JsObject, appr:Approval[User]) = {
    for {
      typ <- (j \ "type").asOpt[String].toRef orIfNone UserError("ListenTo had no type")
      h <- ltHandlers.find(_.fromJson.isDefinedAt((typ, j, appr))).toRef orIfNone UserError(s"I don't know how to listen to ${typ}")
      lt <- h.fromJson(typ, j, appr)
    } yield lt
  }
}


trait ListenToJsonHandler {
  def fromJson:PartialFunction[(String, JsObject, Approval[User]), Ref[ListenTo]]
}

object ChatStreamLTJH extends ListenToJsonHandler {
  
  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  def fromJson = { case ("course", j, appr) =>
    for {
      courseId <- (j \ "courseId").asOpt[Id[Course,String]].toRef
      approved <- appr ask Permissions.readCourse(courseId.lazily)
    } yield ChatStream(courseId)
    
  }
}