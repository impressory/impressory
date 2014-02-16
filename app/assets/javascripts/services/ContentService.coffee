define(["modules/base"], () ->

  Impressory.Services.ContentService = Impressory.angularApp.service('ContentService', ['$http', '$location', '$cacheFactory', '$q', ($http, $location, $cacheFactory, $q) ->
   
    cache = $cacheFactory('contentServiceCache')
    
    Impressory.Caches["contentServiceCache"] = cache

    # Wraps an HTTP call, so that we just get the data (or the error data) rather than the HTTP response
    wrapHttpCall = (call) ->
      call.then(
        (res) -> res.data,
        (errRes) -> $q.reject(errRes.data || { error: "Unexpected error" } )
      )
   
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
        
      myDrafts: (courseId) -> 
        $http.get("/course/#{courseId}/myDrafts").then(
          (res) -> 
            for entry in res.data
              do (entry) -> 
                deferred = $q.defer()
                cache.put(entry.id, deferred.promise)
                deferred.resolve(entry)
            res.data
          ,
          (res) -> $q.reject(res.data)
        
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
        
      allEntries: (courseId) -> $http.get("/course/#{courseId}/allEntries").then((res) -> res.data)
      
      entriesForTopic: (courseId, topic) -> $http.get("/course/#{courseId}/entriesForTopic", { params: { topic : topic } }).then((res) -> res.data)
        
      activity: (courseId) -> $http.get("/course/#{courseId}/activity").then((res) -> res.data)

      # Asks the server what the pasted URL is
      whatIsIt: (code) -> wrapHttpCall(
        $http.get("/whatIsIt", { params: { code : code } })
      )
      
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
      viewerPartialUrl: (kind) -> "/mainPartial?kind=#{kind}"          

      # Identifies the viewer component to include, depending on the type of content.
      # The returned string is the path to the Angular.js partial template.
      streamPartialUrl: (kind) -> "/streamPartial?kind=#{kind}"          
          
      # Identifies the editor component to include, depending on the type of content.
      # The returned string is the path to the Angular.js partial template.
      editPartialUrl: (kind) -> "/editPartial?kind=#{kind}"          
    }
  ])


)