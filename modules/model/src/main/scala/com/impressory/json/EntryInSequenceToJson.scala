package com.impressory.json

import com.impressory.api._
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.RefMany._
import play.api.libs.json._
import com.wbillingsley.handyplay.JsonConverter

object EntryInSequenceToJson extends JsonConverter[EntryInSequence, User] {
  
  def toJson(eis:EntryInSequence) = {
    for (
      entry <- ContentEntryToJson.toJson(eis.entry)
    ) yield Json.obj(
      "entry" -> entry,
      "seqIndex" -> eis.index.orElse(Some(-1))
    )
  }

  def toJsonFor(eis:EntryInSequence, appr: Approval[User]) = {
    for (
      entry <- ContentEntryToJson.toJsonFor(eis.entry, appr)
    ) yield Json.obj(
      "entry" -> entry,
      "seqIndex" -> eis.index.orElse(Some(-1))
    )
  }
}