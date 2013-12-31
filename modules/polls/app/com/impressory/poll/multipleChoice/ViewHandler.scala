package com.impressory.poll.multipleChoice

import com.impressory.plugins._

/**
 * Views
 */
object ViewHandler extends ContentItemViewHandler {
    def main = { case "multipleChoicePoll.html" => views.html.com.impressory.poll.multipleChoice.main().body } 
  
    def stream = { case "multipleChoicePoll.html" => views.html.com.impressory.poll.multipleChoice.stream().body }
  
    def edit = { case "multipleChoicePoll.html" => views.html.com.impressory.poll.multipleChoice.edit().body }
    
    def event:EventViews.handler = { case "Multiple choice poll" => views.html.com.impressory.poll.multipleChoice.pushToChat().body }
}