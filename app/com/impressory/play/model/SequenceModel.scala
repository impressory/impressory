package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import Permissions._
import play.api.libs.json._
import com.impressory.api.UserError

/**
 * From PresentationModel in the previous version
 */
object SequenceModel {
  
  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def create(course:Ref[Course], approval:Approval[User], including:Ref[ContentEntry]) = {
    for (
      a <- approval ask AddContent(course);
      ce <- ContentEntry.unsaved(course, approval.who)
    ) yield {            
      val s = new ContentSequence
      s._entries = including.getId.toSeq
      ce.item = Some(s)
      ce.setPublished(true)
      ce
    }
  }

  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def create(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    val including = Ref.fromOptionId(classOf[ContentEntry], (data \ "item" \ "including").asOpt[String]) 
    
    ce.setPublished(true)
    
    val s = new ContentSequence
    s._entries = including.getId.toSeq
    s
  }
  
  /**
   * Performs updates to a content sequence
   */
  def updateItem(e:ContentEntry, data:JsValue):Ref[ContentEntry] = {
    e.item match {
      case Some(cs: ContentSequence) => {
        (for (ce <- Ref.fromOptionId(classOf[ContentEntry], (data \ "append").asOpt[String])) yield {
          cs._entries = cs._entries :+ ce.id
          e
        }) orIfNone e.itself
      }
      case _ => RefFailed(new UserError("Attempted to edit something that wasn't a ContentSequence as if it was"))
    }
  }

}