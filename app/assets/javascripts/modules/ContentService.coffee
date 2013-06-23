define(["./app"], () ->

  Impressory.angularApp.service('ContentService', ['$http', 'viewingContent', ($http, viewingContent) ->
   
    {
    
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