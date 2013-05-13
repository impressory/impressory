package com.impressory.play

/**
 * 
 */

import play.api.libs.json.{Writes, JsString, JsNull}
import reactivemongo.bson.BSONObjectID
import com.impressory.reactivemongo.HasBSONId
import com.wbillingsley.handy.Ref


package object model {
  
  import _root_.com.impressory.reactivemongo
  
  /*
   * For the moment, we're using package type aliases to direct
   * particular types to particular database implementations.
   * 
   * This is a temporary measure, as code is ported from the
   * previous structure (The Intelligent Book)
   */
  type User = reactivemongo.User
  val User = reactivemongo.User

  type Identity = reactivemongo.Identity
  val Identity = reactivemongo.Identity
  
  type Course = reactivemongo.Course
  val Course = reactivemongo.Course
  
  type Registration = reactivemongo.Registration
  val Registration = reactivemongo.Registration

  type ContentEntry = reactivemongo.ContentEntry
  val ContentEntry = reactivemongo.ContentEntry
  
  type ContentSequence = reactivemongo.ContentSequence
  val ContentSequence = reactivemongo.ContentSequence
  
  type WebPage = reactivemongo.WebPage
  val WebPage = reactivemongo.WebPage
  
  type ChatComment = reactivemongo.ChatComment
  val ChatComment = reactivemongo.ChatComment
  
  type RecordedChatEvent = reactivemongo.RecordedChatEvent
  
  
  type GoogleSlides = reactivemongo.GoogleSlides
  val GoogleSlides = reactivemongo.GoogleSlides
  type YouTubeVideo = reactivemongo.YouTubeVideo
  val YouTubeVideo = reactivemongo.YouTubeVideo
  
  implicit object bsonWrites extends Writes[BSONObjectID] {
    def writes(id:BSONObjectID) = JsString(id.stringify)
  }
  
  implicit def RefBSONWrites[T <: HasBSONId] = new Writes[Ref[T]] {
    def writes(r:Ref[T]) = {
      val str = r.getId(HasBSONId.GetsBSONId).map(_.stringify)
      str.map(JsString(_)).getOrElse(JsNull)
    }
  }
  
}