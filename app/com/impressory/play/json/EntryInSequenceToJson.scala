package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import RefMany._
import play.api.libs.json._

import JsonConverters._

object EntryInSequenceToJson {
  
  def toJson(eis:EntryInSequence) = {
    for (
      entry <- eis.entry.toJson
    ) yield Json.obj(
      "entry" -> entry,
      "seqIndex" -> eis.index.orElse(Some(-1))
    )
  }

  def toJsonFor(eis:EntryInSequence, appr: Approval[User]) = {
    for (
      entry <- eis.entry.toJsonFor(appr)
    ) yield Json.obj(
      "entry" -> entry,
      "seqIndex" -> eis.index.orElse(Some(-1))
    )
  }
}