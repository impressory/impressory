define(["./app"], () ->

  Impressory.angularApp.service('viewingCourse', ['$http', '$location', ($http, $location) ->
      
    viewing = Impressory.Model.Viewing
    
    getting = { }
   
    {
    
      # In case we've already initiated a request to fetch something
      alreadyInFlight: (kind, id) -> 
        getting[{ kind : kind, id : id }] ? null
    
      # Performs the fetching of a course
      fetchCourse: (courseId) -> 
        promise = $http.get("/course/" + courseId).then((res) ->
          viewing.Course = res.data
          delete getting[{ kind : 'course', id : courseId }]
          viewing.Course 
        )         
        getting[{ kind : 'course', id : courseId }] = promise
        promise
    
      # Gets the course, from JSON, the current in-flight request, or requesting it
      get: (courseId) ->
        if (viewing.Course?.id == courseId)
          viewing.Course
        else 
          @alreadyInFlight("course", courseId) or @fetchCourse(courseId)
          
          
      #--- Content ---#
          
          
      # Gets content, from JSON or requesting it
      queryContent: (params) ->
        if (params.entryId?)
          @contentFromViewing(params["courseId"], params["entryId"]) || @fetchContent(params)
        else 
          @fetchContent(params)
          
      # Tries to find the requested content in Model.Viewing.Content
      contentFromViewing: (courseId, entryId) ->
        if (viewing.Course?.id == courseId)
          if (entryId? and viewing.Content.entry?.id == entryId)
            viewing.Content.seqIndex = -1
            @updateDisplayedEntry()
          else if (entryId? and viewing.Content.entry?.item?.entries?.indexOf(entryId) >= 0)
            viewing.Content.seqIndex = viewing.Content.entry?.item?.entries?.indexOf(entryId)
            @updateDisplayedEntry()
          else
            null
        else
          null
        
      
      # Calls the server to query for content, returning a promise to look up the new current entry
      fetchContent: (params) ->
        courseId = params.courseId ? viewing.Course?.id
        promise = $http.get("/course/" + courseId + "/content", { params: params }).then((res) =>
          viewing.Content = res.data
          @updateLocation(true)
          @updateDisplayedEntry()
        , (erres) =>
          if (erres.data?.error == "not found") 
            viewing.Content.entry = {}
            @updateDisplayedEntry()
        )
          
      # Gets the current entry (which may be an entry in a sequence)
      currentEntry: () ->
        if (viewing.Content?.entry?.kind == "sequence" and viewing.Content?.seqIndex >= 0)
          viewing.Content.entry.item?.entries[viewing.Content.seqIndex]
        else
          viewing.Content.entry
          
      # Updates viewing.Content.display
      updateDisplayedEntry: () ->
        viewing.Content.display = @currentEntry()
        viewing.Content.display
          
      # Updates the location (URL) based on the data that has been set as being viewed
      updateLocation: (replace) ->
        c = @updateDisplayedEntry()
        if c?.course? and c.id?
          if (replace) then $location.replace()
          $location.path("/course/" + c.course + "/viewContent").search({ entryId: c.id })
    }
  ])


)