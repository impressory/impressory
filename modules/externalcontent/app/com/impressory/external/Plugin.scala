package com.impressory.external

object Plugin {
  
  def onStart() {
    com.impressory.json.ContentItemToJson.registerHandler(GoogleSlides.JsonHandler)
    com.impressory.reactivemongo.ContentItemToBson.registerHandler(GoogleSlides.BsonHandler)
    com.impressory.plugins.ContentItemViews.registerHandler(GoogleSlides.ViewHandler)
    
    com.impressory.json.ContentItemToJson.registerHandler(YouTubeVideo.JsonHandler)
    com.impressory.reactivemongo.ContentItemToBson.registerHandler(YouTubeVideo.BsonHandler)
    com.impressory.plugins.ContentItemViews.registerHandler(YouTubeVideo.ViewHandler)

    com.impressory.json.ContentItemToJson.registerHandler(WebPage.JsonHandler)
    com.impressory.reactivemongo.ContentItemToBson.registerHandler(WebPage.BsonHandler)
    com.impressory.plugins.ContentItemViews.registerHandler(WebPage.ViewHandler)
  }

}