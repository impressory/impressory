package com.impressory.builtincontent

import com.impressory.builtincontent.markdown.MarkdownPageModel
import com.impressory.builtincontent.sequence.SequenceModel

object Plugin {

  def onStart() {
    com.impressory.json.ContentItemToJson.registerHandler(MarkdownPageModel.JsonHandler)
    com.impressory.reactivemongo.ContentItemToBson.registerHandler(MarkdownPageModel.BsonHandler)
    com.impressory.plugins.ContentItemViews.registerHandler(MarkdownPageModel.ViewHandler)

    com.impressory.json.ContentItemToJson.registerHandler(SequenceModel.JsonHandler)
    com.impressory.reactivemongo.ContentItemToBson.registerHandler(SequenceModel.BsonHandler)
    com.impressory.plugins.ContentItemViews.registerHandler(SequenceModel.ViewHandler)

  }

}
