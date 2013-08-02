package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import RefMany._
import play.api.libs.json._

import JsonConverters._

object ContentSequenceToJson {

    def toJson(cs:ContentSequence) = {
      // This ensures we don't go into an infinite loop if a sequence has somehow included its own item
      val filteredEntries = cs.entries.withFilter(_.item match {
          case Some(cs:ContentSequence) => false;
          case _ => true
        });
      
      for (
        entries <- filteredEntries.flatMap(_.toJson).toRefOne
      ) yield {
        Json.obj(
        "entries" -> entries.toSeq)
      }
    }
    
    def toJsonFor(cs:ContentSequence, appr:Approval[User]) = {
      for (
        entries <- cs.entries.withFilter(_.kind != ContentSequence.itemType).flatMap(_.toJsonFor(appr)).toRefOne
      ) yield Json.obj(
        "entries" -> entries.toSeq)      
    }  
  
}