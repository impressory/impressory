package com.impressory.json

import com.impressory.api._

import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.RefMany._

import play.api.libs.json._

object ContentSequenceToJson {

    def toJson(cs:ContentSequence) = {
      // This ensures we don't go into an infinite loop if a sequence has somehow included its own item
      val filteredEntries = cs.entries.withFilter(_.item match {
          case Some(cs:ContentSequence) => false;
          case _ => true
        });
      
      for {
        entries <- filteredEntries.flatMap(ContentEntryToJson.toJson(_)).toRefOne
      } yield {
        Json.obj(
        "entries" -> entries.toSeq)
      }
    }
    
    def toJsonFor(cs:ContentSequence, appr:Approval[User]) = {
      for (
        entries <- cs.entries.withFilter(_.kind != ContentSequence.itemType).flatMap(ContentEntryToJson.toJsonFor(_, appr)).toRefOne
      ) yield Json.obj(
        "entries" -> entries.toSeq)      
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
        
        val verifiedIds = for {
          entry <- new RefManyById(classOf[ContentEntry], entryIds) if (entry.course.getId == e.course.getId)
        } yield entry.id
        
        for (
          seq <- verifiedIds.toRefOne
        ) yield {
          e.copy(item = Some(cs.copy(entries = new RefManyById(classOf[ContentEntry], seq.toSeq))))
        }
      }
      case _ => RefFailed(new UserError("Attempted to edit something that wasn't a ContentSequence as if it was"))
    }
  }    
}