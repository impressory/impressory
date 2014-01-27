define(["./base", "services/ContentService"], () ->


  # viewingContent is responsible for maintaining what is shown in the content viewer
  # In other words, it maintains Impressory.Model.Viewing.Content
  #
  #
  Impressory.angularApp.service('viewingContent', ['$location', '$rootScope', '$window', ($location, $rootScope, $window) ->
      
    {
      # Tries to find the entry with this id in the currently viewed entry or (if
      # it's a sequence) in its entries. Always sets goToSeqIndex.
      tryToVisitInViewing: (entryId) ->
        viewing = Impressory.Model.Viewing.Content
        
        # Check to see if we're already looking at it
        if (viewing.entry?.id == entryId) 
          viewing.goToSeqIndex = -1
          viewing.entry
        else if (viewing.entry?.item?.entries?)
          idx = viewing.entry?.item?.entries?.indexOf(entryId)
          if (idx >= 0) 
            viewing.goToSeqIndex = idx
            viewing.entry
          else
            viewing.goToSeqIndex = -1
            null        
        else
          viewing.goToSeqIndex = -1
          null        
      
    }
  ])


)