define(["./app"], () ->

  Impressory.angularApp.service('CourseService', ['$http', '$cacheFactory', ($http, $cacheFactory) ->
   
    cache = $cacheFactory("courseCache")
   
    {
      get: (courseId) ->
        cache.get(courseId) || ( 
          prom = $http.get("/course/#{courseId}").then((res) -> res.data)
          cache.put(courseId, prom)
          prom
        )
        
      
      save: (course) -> $http.post("/course/#{course.id}/update", course).then((res) ->
        cache.put(course.id, res.data) 
        res.data
      )
      
      fetchInvites: (courseId) -> $http.get("/course/#{courseId}/invites").then((res) -> res.data)
      
      createInvite: (courseId, data) -> $http.post("/course/#{courseId}/createInvite", data).then((res) -> res.data)
      
      useInvite: (courseId, codePacket) -> $http.post("/course/#{courseId}/useInvite", codePacket).then((res) -> res.data)
    }
  ])


)