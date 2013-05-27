package com.impressory.reactivemongo

import reactivemongo.bson.{BSONDocument, BSONDocumentWriter, BSONDocumentReader}

class MarkdownPage(
  var text:String, 
  var version:Int = 0
) extends ContentItem {
  
  val itemType = MarkdownPage.itemType
}

object MarkdownPage {
  
  val itemType = "Markdown page"
    
  implicit object bsonWriter extends BSONDocumentWriter[MarkdownPage] {
    def write(mp: MarkdownPage) = BSONDocument(
        "text" -> mp.text, "version" -> mp.version
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[MarkdownPage] {
    def read(doc: BSONDocument): MarkdownPage = {
      val page = new MarkdownPage(
        text = doc.getAs[String]("text").get,
        version = doc.getAs[Int]("version").get
      )
      page
    }
  }  
  
}

