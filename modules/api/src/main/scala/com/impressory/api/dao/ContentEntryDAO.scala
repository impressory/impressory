package com.impressory.api.dao

import com.impressory.api.{Course, ContentEntry}
import com.wbillingsley.handy._

trait ContentEntryDAO {

  def lookUp:LookUp[ContentEntry,String]

  def byTopic(course:RefWithId[Course], topic:String):RefMany[ContentEntry]

  def inIndexByCourse(course:RefWithId[Course]):RefMany[ContentEntry]

  def recentInNewsByCourse(course:RefWithId[Course]):RefMany[ContentEntry]

  def sequencesContaining(ce:RefWithId[ContentEntry]):RefMany[ContentEntry]

  def saveNew(ce:ContentEntry):Ref[ContentEntry]

}

object NullContentEntryDAO extends ContentEntryDAO {

  private val msg = "No ContentEntryDAO has been configured"

  private def failed = RefFailed(new IllegalStateException(msg))

  def lookUp = LookUp.fails(msg)

  def byTopic(course:RefWithId[Course], topic:String) = failed

  def inIndexByCourse(course:RefWithId[Course]) = failed

  def recentInNewsByCourse(course:RefWithId[Course]) = failed

  def sequencesContaining(ce:RefWithId[ContentEntry]) = failed

  def saveNew(ce:ContentEntry) = failed

}