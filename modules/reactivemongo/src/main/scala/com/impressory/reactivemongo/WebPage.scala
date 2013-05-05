package com.impressory.reactivemongo

import com.wbillingsley.handy._
import Ref._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import com.impressory.api._

import play.api.libs.concurrent.Execution.Implicits._

class WebPage (
    
  val _course:Option[BSONObjectID] = None,

  val _ce:Option[BSONObjectID] = None,

  var url:Option[String] = None,

  var noFrame:Boolean = false,
  
  val _id: BSONObjectID = BSONObjectID.generate
  
) extends HasBSONId with ContentItem {
  
  val itemType = WebPage.itemType
  
  def id = _id
  
  def course = new RefById(classOf[Course], _course)
  
  def ce = new RefById(classOf[ContentEntry], _ce)
  
}

object WebPage extends FindById[WebPage] {
  
  val itemType = "web page"
  
  val collName = "contentEntry"
    
  def unsaved(course:Ref[Course], ce:Ref[ContentEntry], url:Option[String]) = {
    new WebPage(course.getId, ce.getId, url)
  }
    
  implicit object bsonWriter extends BSONDocumentWriter[WebPage] {
    def write(wp: WebPage) = BSONDocument(
        "_id" -> wp._id, "_ce" -> wp._ce, "_course" -> wp._course,
        "url" -> wp.url, "noFrame" -> wp.noFrame
    )
  }
  
  implicit object bsonReader extends BSONDocumentReader[WebPage] {
    def read(doc: BSONDocument): WebPage = {
      val page = new WebPage(
        _course = doc.getAs[BSONObjectID]("_course"),
        _ce = doc.getAs[BSONObjectID]("_ce"),
        url = doc.getAs[String]("url"),
        noFrame = doc.getAs[Boolean]("noFrame").getOrElse(false),
        _id = doc.getAs[BSONObjectID]("_id").get
        )
      page
    }
  }
  
  
}