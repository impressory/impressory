define(["./app"], () ->

  Impressory.angularApp.service('viewingContent', ['$http', '$location', '$rootScope', '$window', ($http, $location, $rootScope, $window) ->
      
    viewing = Impressory.Model.Viewing
      
    $(window).on('resize', () ->
      $rootScope.wh = $window.innerHeight
      $rootScope.$apply()
    )

    $rootScope.wh = $window.innerHeight    
   
    {
      refreshHeight: () -> 
        $rootScope.wh = $window.innerHeight

      # Looks through the current entry (assuming it's a sequence) to find within it
      # the entry with the specified ID.
      slideIndex: (id) ->
        items = viewing.Content.entry?.item?.entries 
        found = -1
        i = 0
        while (found < 0 and i < items?.length)
          if items[i].id == id
            found = i
          i++
        found
          
      # Tries to find the requested content in Model.Viewing.Content
      contentFromViewing: (entryId) ->
        if (entryId? and viewing.Content.entry?.id == entryId)
          viewing.Content.seqIndex = -1
          @updateDisplayedEntry()
        else
          sIndex = @slideIndex(entryId) 
          if (entryId? and sIndex >= 0)
            viewing.Content.seqIndex = sIndex
            @updateDisplayedEntry()
          else
            null
      
      
      # Tries to find the given entry in the viewing set
      findEntryInViewing: (entryId) -> 
        if (entryId? and viewing.Content.entry?.id == entryId)
          viewing.Content.entry
        else
          sIndex = @slideIndex(entryId)
          if (sIndex >= 0)
            viewing.Content.entry.item.entries(sIndex)
          else 
            null
          
      
      # Calls the server to query for content, returning a promise to look up the new current entry
      fetchContent: (params) ->
        courseId = params.courseId ? viewing.Course?.id
        promise = $http.get("/course/" + courseId + "/content", { params: params }).then((res) =>
          @viewThisData(res.data)
        , (erres) =>
          if (erres.data?.error == "not found") 
            viewing.Content.entry = null
            @updateDisplayedEntry()
        )
        
      # Updates Impressory.Model.Viewing.Content with the given data, and then computes what to view 
      viewThisData: (data) -> 
        viewing.Content = data
        @updateDisplayedEntry()
      
      
      # Attempts to update the data of an entry; if it's not in the vewing set, just views it.
      updateEntryInPlace: (entry) ->
        inview = @findEntryInViewing(entry.id)
        if (inview)
          angular.copy(entry, inview)
          @updateDisplayedEntry()
        else
          @viewThisData({
            entry: entry
            seqIndex: -1
          })
          
      # Gets the current entry (which may be an entry in a sequence)
      currentEntry: () ->
        if (viewing.Content?.entry?.kind == "sequence" and viewing.Content?.seqIndex >= 0)
          viewing.Content.entry.item?.entries[viewing.Content.seqIndex]
        else
          viewing.Content.entry

      # Updates the sequence of items (entry + entry.item.entries) that may be displayed 
      updateDisplaySeq: () ->
        item = viewing.Content.entry?.item
        newVal = if item?.entries?
          [ viewing.Content.entry ].concat(item.entries)
        else 
          if viewing.Content.entry? then [ viewing.Content.entry ] else []
        viewing.Content.displaySeq = newVal
         
         
      # Updates viewing.Content.display
      updateDisplayedEntry: () ->
        viewing.Content.display = @currentEntry()
        @updateDisplaySeq()
        @updateLocation()
        viewing.Content.noContent = viewing.Content.displaySeq.length <= 0
        viewing.Content.display
          
      # Updates the location (URL) based on the data that has been set as being viewed
      updateLocation: () ->
        oldSearch = $location.search()
        if viewing.Content.display?.id != oldSearch.entryId
          if not oldSearch.entryId?
            $location.replace()
          $location.search({ entryId: viewing.Content.display.id })
          
      goToStart: () -> 
        viewing.Content.seqIndex = -1
        @updateDisplayedEntry()
        
      goToNextEntry: () ->
        if viewing.Content.seqIndex < viewing.Content.displaySeq.length - 2
          viewing.Content.seqIndex = viewing.Content.seqIndex + 1
          @updateDisplayedEntry()
          
      goToPrevEntry: () ->
        if viewing.Content.seqIndex >= 0
          viewing.Content.seqIndex = viewing.Content.seqIndex - 1
          @updateDisplayedEntry()

      # Gets content, from JSON or requesting it
      lookUp: (params) ->
        if (params.entryId?)
          @contentFromViewing(params["entryId"]) || @fetchContent(params)
        else 
          @fetchContent(params)

    }
  ])


)