package com.impressory.api

case class MarkdownPage(
  var text:String, 
  var version:Int = 0
) extends ContentItem {
  
  val itemType = MarkdownPage.itemType
}

object MarkdownPage {
  
  val itemType = "Markdown page"
  
}