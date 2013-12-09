package com.impressory.reactivemongo

import com.wbillingsley.handy.RefManyById

import reactivemongo.api._
import reactivemongo.bson._

import com.impressory.api._

object ContentSequenceWriter extends BSONDocumentWriter[ContentSequence] {
  
  import UserDAO.RefManyByIdWriter
  
  def write(s: ContentSequence) = {
    val doc = BSONDocument(
      "entries" -> s.entries
      )
    doc
  }
}
  
object ContentSequenceReader extends BSONDocumentReader[ContentSequence] {
  def read(doc: BSONDocument): ContentSequence = {
    new ContentSequence(
      entries = doc.getAs[RefManyById[ContentEntry, String]]("entries").getOrElse(new RefManyById(classOf[ContentEntry], Seq.empty))
    )
  }
}
  

