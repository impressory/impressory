package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._

import com.impressory.api._
import com.impressory.json._
import com.impressory.plugins._

object MarkdownPageModel extends ContentItemJsonHandler {

  var defaultText = "The default text for Markdown Pages has not been set yet"
  
  def urlChecker(blank:ContentEntry, url:String) = RefNone
    
  def toJsonFor = { case (entry, mp: MarkdownPage, appr) => 
    Json.obj(
      "text" -> mp.text,
      "version" -> mp.version
    ).itself
  }
  
  def createFromJson= { case (MarkdownPage.itemType, json, blank) =>
    blank.copy(
      tags = blank.tags.copy(nouns=blank.tags.nouns + "Wiki page", site=None),
      item = Some(new MarkdownPage((json \ "item" \ "text").asOpt[String] getOrElse defaultText))
    ).itself
  }

  def updateFromJson = { case (MarkdownPage.itemType, json, before) =>
    val text = (json \ "item" \ "text").asOpt[String]
    val version = (json \ "item" \ "version").asOpt[Int].getOrElse(0)

    before.item match {
      case Some(mp: MarkdownPage) => {
        if (mp.version == version) {
          for (t <- text) {
            mp.text = t
            mp.version = mp.version + 1
          }
          before.itself
        } else {
          RefFailed(UserError("The version of the page has changed"))
        }
      }
      case _ => RefFailed(new IllegalStateException("Tried to update a non-Markdown item as if it was a Markdown page"))
    }
  }

}