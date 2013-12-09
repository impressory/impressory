package com.impressory.api.external

import com.impressory.api._

case class WebPage (
    
  var url:Option[String] = None,

  var noFrame:Boolean = false
  
) extends ContentItem {
  
  val itemType = WebPage.itemType
  
}

object WebPage {
  
  val itemType = "web page"
  
}