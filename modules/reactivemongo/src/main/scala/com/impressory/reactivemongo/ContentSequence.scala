package com.impressory.reactivemongo

import com.wbillingsley.handy.RefManyById

import reactivemongo.api._
import reactivemongo.bson._

import com.impressory.api._

object ContentSequenceHandler extends ContentItemBsonHandler {
  
  // Import the configuration to create RefByIds (where to look them up)
  import com.impressory.plugins.LookUps._
  
  import UserDAO.RefManyByStringIdWriter
  
  def create = { case s:ContentSequence => BSONDocument("entries" -> s.entries) }
  
  def update = { case s:ContentSequence => BSONDocument("item.entries" -> s.entries) }
  
  def read = { case (ContentSequence.itemType, doc) => 
    new ContentSequence(
      entries = doc.getAs[RefManyById[ContentEntry, String]]("entries").getOrElse(RefManyById.empty)
    )
  }
  
}

