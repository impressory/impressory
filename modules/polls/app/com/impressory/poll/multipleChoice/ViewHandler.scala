package com.impressory.poll.multipleChoice

import com.impressory.plugins._

/**
 * Views
 */
object ViewHandler extends ContentItemViewHandler {
    def main = { case MultipleChoicePoll.itemType => views.html.com.impressory.poll.multipleChoice.main().body } 
  
    def stream = { case MultipleChoicePoll.itemType => views.html.com.impressory.poll.multipleChoice.stream().body }
  
    def edit = { case MultipleChoicePoll.itemType => views.html.com.impressory.poll.multipleChoice.edit().body }
    
    def event:EventViews.handler = { case MultipleChoicePoll.itemType => views.html.com.impressory.poll.multipleChoice.pushToChat().body }
}