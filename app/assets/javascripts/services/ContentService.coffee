define(["modules/base"], () ->

  Impressory.Services.ContentService = Impressory.angularApp.service('ContentService', ['$http', '$location', '$cacheFactory', '$q', ($http, $location, $cacheFactory, $q) ->
   
    cache = $cacheFactory('contentServiceCache')
    
    Impressory.Caches["contentServiceCache"] = cache
   
    {
      # Returns a promise containing the JSON of a content entry
      get: (courseId, entryId) ->
        cache.get(entryId) || (
          promise =  $http.get("/course/#{courseId}/entry/#{entryId}").then((res) -> 
            console.log(res.data)
            res.data
          )
          cache.put(entryId, promise)
          promise
        )
        
      request: (courseId, ids) ->
      
        # Eliminate the ids we have already fetched
        unfetched = (x for x in ids when !(cache.get(x)?))
        
        # If there are any left to request, request them
        if unfetched.length > 0
          
          deferred = []
          
          # put a new promise into the cache, because these are being fetched
          for unfetchedId in unfetched
            do (unfetchedId) -> 
              deferred[unfetchedId] = $q.defer()
              cache.put(unfetchedId, deferred[unfetchedId].promise)
            
          # fetch them and fulfill the promises
          $http.post("/course/#{courseId}/entriesByIds", { ids: unfetched }).then((res) ->
            for entry in res.data 
              do (entry) ->
                deferred[entry.id]?.resolve(entry)
          )
          
        # We now have promises for every id
        promises = (cache.get(id) for id in ids)
        
        # Combine them all as a single promise to return
        $q.all(promises)
        
        
      # Calls the server to query for content, returning a promise to look up the new current entry
      # in sequence (an EntryInSequence structure)
      lookUp: (courseId, params) ->
        $http.get("/course/" + courseId + "/content", { params: params }).then((res) =>
          res.data
        , (errRes) =>
          errRes.data?.error || "Unexpected error looking up content"
        )
    
      viewPath: (courseId, entryId) -> "/course/#{courseId}/view/#{entryId}"
      
      viewEntryPath: (entry) -> @viewPath(entry.course, entry.id)
    
      viewUrl: (courseId, entryId) -> 
        if $location.port() == 80
          "#{$location.protocol()}://#{$location.host()}#{@viewPath(courseId, entryId)}"
        else
          "#{$location.protocol()}://#{$location.host()}:#{$location.port()}#{@viewPath(courseId, entryId)}"
    
      viewEntryUrl: (entry) -> @viewUrl(entry.course, entry.id)
    
      embedUrl: (courseId, entryId) -> 
        if $location.port() == 80
          "#{$location.protocol()}://#{$location.host()}/course/#{courseId}/embedContent?entryId=#{entryId}"
        else
          "#{$location.protocol()}://#{$location.host()}:#{$location.port()}/course/#{courseId}/embedContent?entryId=#{entryId}"
    
      voteDown: (entry) ->
        $http.post("/course/#{entry.course}/entry/#{entry.id}/voteDown").then((res) ->          
          angular.copy(res.data, entry)
        )

      voteUp: (entry) ->
        $http.post("/course/#{entry.course}/entry/#{entry.id}/voteUp").then((res) ->
          angular.copy(res.data, entry)
        )
        
      addComment: (entry, text) ->  
        $http.post("/course/#{entry.course}/entry/#{entry.id}/addComment", { text: text }).then((res) ->
          angular.copy(res.data, entry)
        )
        
      allEntries: (courseId) -> $http.get("/course/#{courseId}/allEntries").then((res) -> res.data.entries)
        
      activity: (courseId) -> $http.get("/course/#{courseId}/activity").then((res) -> res.data)
      
      whatIsIt: (code) -> $http.get("/whatIsIt", { params: { code : code } })
      
      addContent: (courseId, entry) -> $http.post("/course/#{courseId}/addContent", entry).then((res) -> res.data)

      # Asks the server to update a content entry's item      
      editItem: (entry) -> $http.post("/course/#{entry.course}/entry/#{entry.id}/editItem", entry).then(
        (res) ->
          entry = res.data 
          ## TODO: update the passed-in entry
          entry 
        (res) -> res.data
      )
      
      # Asks the server to update a content entry's tags, settings, and metadata      
      editTags: (entry) -> $http.post("/course/#{entry.course}/entry/#{entry.id}/editTags", entry).then(
        (res) -> 
          entry = res.data 
          ## TODO: update the passed-in entry
          entry 
        (res) -> res.data
      )
        
      # Identifies the viewer component to include, depending on the type of content.
      # The returned string is the path to the Angular.js partial template.
      viewerPartialUrl: (kind) -> 
        switch kind
          when 'Multiple choice poll' then '/partials/viewcontent/kinds/multipleChoicePoll.html'
          when 'Markdown page' then '/partials/viewcontent/kinds/markdownPage.html'
          when 'Google Slides' then '/partials/viewcontent/kinds/googleSlides.html'
          when 'sequence' then '/partials/viewcontent/kinds/contentSequence.html'
          when 'web page' then '/partials/viewcontent/kinds/webPage.html'
          when 'YouTube video' then '/partials/viewcontent/kinds/youTubeVideo.html'
          else '/partials/viewcontent/kinds/noContent.html'

      # Identifies the viewer component to include, depending on the type of content.
      # The returned string is the path to the Angular.js partial template.
      streamPartialUrl: (kind) -> 
        switch kind
          when 'YouTube video' then '/partials/viewcontent/stream/youTubeVideo.html'
          when 'Markdown page' then '/partials/viewcontent/stream/markdownPage.html'
          when 'Multiple choice poll' then '/partials/viewcontent/stream/multipleChoicePoll.html'
          else '/partials/viewcontent/stream/default.html'
          
          
      # Identifies the editor component to include, depending on the type of content.
      # The returned string is the path to the Angular.js partial template.
      editPartialUrl: (kind) -> 
        switch kind
          when 'sequence' then '/partials/editcontent/kinds/contentSequence.html'
          when 'Google Slides' then '/partials/editcontent/kinds/googleSlides.html'
          when 'Markdown page' then '/partials/editcontent/kinds/markdownPage.html'
          when 'Multiple choice poll' then '/partials/editcontent/kinds/multipleChoicePoll.html'
          when 'web page' then '/partials/editcontent/kinds/webPage.html'
          when 'YouTube video' then '/partials/editcontent/kinds/youTubeVideo.html'
          else '/partials/editcontent/kinds/default.html'          
    }
  ])


)