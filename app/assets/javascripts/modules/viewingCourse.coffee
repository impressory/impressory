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
          
    }
  ])


)