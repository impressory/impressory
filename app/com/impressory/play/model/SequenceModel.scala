package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import Permissions._
import play.api.libs.json._

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
      ce <- ContentEntry.unsaved(course, approval.who, kind=Some(ContentSequence.itemType))
    ) yield {            
      val s = new ContentSequence
      s._entries = including.getId.toSeq
      ce.item = Some(s)
      ce
    }
  }

  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def create(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    val including = Ref.fromOptionId(classOf[ContentEntry], (data \ "item" \ "including").asOpt[String]) 
    
    println(s"Including is $including")
    
    val s = new ContentSequence
    s._entries = including.getId.toSeq
    s
  }

}