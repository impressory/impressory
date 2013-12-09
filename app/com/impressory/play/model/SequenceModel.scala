package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.security.Permissions
import Permissions._
import com.impressory.reactivemongo.ContentEntryDAO

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
      ce = ContentEntryDAO.unsaved.copy(course=course, addedBy=approval.who)
    ) yield {            
      val s = new ContentSequence(
        entries = new RefManyById(classOf[ContentEntry], including.getId.toSeq)
      )
      ce.setPublished(true)
      ce.copy(item = Some(s))
    }
  }

  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def create(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    val including = Ref.fromOptionId(classOf[ContentEntry], (data \ "item" \ "including").asOpt[String]) 
    val s = new ContentSequence(
        entries = new RefManyById(classOf[ContentEntry], including.getId.toSeq)
      )
    ce.setPublished(true)
    ce.copy(item = Some(s))
  }

}