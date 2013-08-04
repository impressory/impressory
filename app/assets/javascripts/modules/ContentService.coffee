define(["./app"], () ->

  Impressory.angularApp.service('ContentService', ['$http', '$location', 'viewingContent', ($http, $location, viewingContent) ->
   
    {
    
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
        
      viewPath: (entry) -> "/course/#{entry.course}/viewContent?entryId=#{entry.id}"
      
      addContent: (courseId, entry) -> $http.post("/course/#{courseId}/addContent", entry )

      # Asks the server to update a content entry's item      
      editItem: (entry) -> $http.post("/course/#{entry.course}/entry/#{entry.id}/editItem", entry).then(
        (res) ->
          entry = res.data 
          viewingContent.updateEntryInPlace(entry)
          entry 
        (res) -> res.data
      )
      
      # Asks the server to update a content entry's tags, settings, and metadata      
      editTags: (entry) -> $http.post("/course/#{entry.course}/entry/#{entry.id}/editTags", entry).then(
        (res) -> 
          entry = res.data 
          viewingContent.updateEntryInPlace(entry)
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
          else 'partials/viewcontent/kinds/noContent.html'
    }
  ])


)