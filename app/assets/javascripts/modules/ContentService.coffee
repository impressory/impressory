define(["./app"], () ->

  Impressory.angularApp.service('ContentService', ['$http', '$location', 'viewingContent', ($http, $location, viewingContent) ->
   
    {
    
      embedUrl: (courseId, entryId) -> 
        if $location.port() == 80
          "#{$location.protocol()}://#{$location.host()}/course/#{courseId}/embedContent?entryId=#{entryId}"
        else
          "#{$location.protocol()}://#{$location.host()}:#{$location.port()}/course/#{courseId}/embedContent?entryId=#{entryId}"
    
      voteDown: (courseId, entryId) ->
        $http.post("/course/#{courseId}/entry/#{entryId}/voteDown").then((res) ->
          viewingContent.updateEntryInPlace(res.data)
        )

      voteUp: (courseId, entryId) ->
        $http.post("/course/#{courseId}/entry/#{entryId}/voteUp").then((res) ->
          viewingContent.updateEntryInPlace(res.data)
        )
        
      addComment: (courseId, entryId, text) ->  
        $http.post("/course/#{courseId}/entry/#{entryId}/addComment", { text: text }).then((res) ->
          viewingContent.updateEntryInPlace(res.data)
        )
        
      allEntries: (courseId) -> $http.get("/course/#{courseId}/allEntries").then((res) -> res.data.entries)
        
      activity: (courseId) -> $http.get("/course/#{courseId}/activity").then((res) -> res.data)
        
      viewPath: (entry) -> "/course/#{entry.course}/viewContent?entryId=#{entry.id}"
        
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