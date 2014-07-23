package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import Ids._
import play.api.libs.json._

import com.impressory.api._
import com.impressory.security.Permissions._
import com.impressory.json._
import com.impressory.plugins._

import LookUps._

/**
 * From PresentationModel in the previous version
 */
object SequenceModel {

  object JsonHandler extends ContentItemJsonHandler {
    def urlChecker(blank:ContentEntry, url:String) = RefNone
    
    def toJsonFor = { case (entry, cs: ContentSequence, appr) => 
      Json.obj(
        "entries" -> cs.entries
      ).itself
    }
  
    def createFromJson= { case (ContentSequence.itemType, json, blank) =>
      val including = (json \ "item" \ "including").asOpt[String]
      val s = new ContentSequence(
        entries = including.toSeq.asIds[ContentEntry]
      )
      blank.copy(
        tags = blank.tags.copy(nouns=blank.tags.nouns + "Sequence", site=None),
        item = Some(s),
        settings = blank.settings.copy(published=Some(System.currentTimeMillis))
      ).itself
    }

    def updateFromJson = { case (ContentSequence.itemType, json, before) =>
      /*
       * When we send a ContentSequence as JSON to the client, we include not just the IDs of the entries
       * but their full JSON.  The client sends them back in the same format.
       * So, we need to extract the IDs from the JSON that the client sent.
       */
      for { 
        entryIds <- (json \ "item" \ "entries").asOpt[Seq[String]].toRef orIfNone UserError("Content entries was missing")
      } yield {
        before.copy(item = Some(ContentSequence(entries = entryIds.asIds[ContentEntry])))
      }
    }
  }
  
  object ViewHandler extends ContentItemViewHandler {
    def main = { case ContentSequence.itemType => views.html.com.impressory.content.contentSequence.main().body } 
  
    def stream = { case ContentSequence.itemType => views.html.com.impressory.content.contentSequence.stream().body } 
  
    def edit = { case ContentSequence.itemType => views.html.com.impressory.content.contentSequence.edit().body }  
  }  

}