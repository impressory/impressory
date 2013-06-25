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
    }
  ])


)