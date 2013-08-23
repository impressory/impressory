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

        /*
         * When we send a ContentSequence as JSON to the client, we include not just the IDs of the entries
         * but their full JSON.  The client sends them back in the same format.
         * So, we need to extract the IDs from the JSON that the client sent.
         */
        val entryIds = data \ "item" \ "entries" \\ "id"
        
        val verifiedIds = for (
          entryId <- entryIds.toRefMany;
          entry <- refContentEntry(entryId.as[String]) if (entry.course.getId == e.course.getId)
        ) yield entry.id
        
        for (
          seq <- verifiedIds.toRefOne
        ) yield {
          cs._entries = seq.toSeq
          e
        }
      }
      case _ => RefFailed(new UserError("Attempted to edit something that wasn't a ContentSequence as if it was"))
    }
  }

}