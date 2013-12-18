package com.impressory.poll

import com.impressory.plugins._
import com.impressory.json._
import com.impressory.eventroom._
import com.impressory.reactivemongo.ContentItemToBson

object Plugin {
  
  def onStart() = {
    
    ContentItemViews.registerHandler(multipleChoice.ViewHandler)
    ContentItemToJson.registerHandler(multipleChoice.JsonHandler)
    ContentItemToBson.registerHandler(multipleChoice.BsonHandler)
    
  }

}