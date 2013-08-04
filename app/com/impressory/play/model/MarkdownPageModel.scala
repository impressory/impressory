package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._
import com.impressory.api.UserError

object MarkdownPageModel {

  var defaultText = "The default text for Markdown Pages has not been set yet"
  
  def toJson(mp:MarkdownPage) = Json.obj(
    "text" -> mp.text,
    "version" -> mp.version
  )
    
  def create(course: Ref[Course], approval: Approval[User], ce: ContentEntry, text: String = defaultText) = {
    ce.tags.site = None
    ce.setPublished(false)
    RefItself(new MarkdownPage(text))
  }

  def updateItem(ce: ContentEntry, data: JsValue) = {
    val text = (data \ "item" \ "text").asOpt[String]
    val version = (data \ "item" \ "version").asOpt[Int].getOrElse(0)

    ce.item match {
      case Some(mp: MarkdownPage) => {
        if (mp.version == version) {
          for (t <- text) {
            mp.text = t
            mp.version = mp.version + 1
          }
          ce.itself
        } else {
          RefFailed(UserError("The version of the page has changed"))
        }
      }
      case _ => RefFailed(new IllegalStateException("Tried to update a non-Markdown item as if it was a Markdown page"))
    }
  }

}