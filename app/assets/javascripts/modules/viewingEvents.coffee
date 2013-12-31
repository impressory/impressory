define(["./base", "services/UserService"], () ->

  Impressory.angularApp.service('viewingEvents', ['$http', '$location', '$rootScope', 'UserService', ($http, $location, $rootScope, UserService) ->
    
    ERData = Impressory.Model.Viewing.EventRoom

    Impressory.EventRoom = EventRoom.create()
    
    Impressory.EventRoom.addListener({ 
      receive: (msg) -> 
        console.log("Received a message")
        switch msg.kind
          when "push"
            ERData.events.push(msg)
            $rootScope.$broadcast(msg.kind, msg)
            $rootScope.$broadcast(msg.type, msg)
          when "state"
            ERData.states[msg.id] = msg.state
            $rootScope.$broadcast(msg.type, msg)
    })
    
   
    {

      showForCourse: (courseId) ->       
        Impressory.EventRoom.connect()
        if (ERData.course != courseId)
          oldCourseId = ERData.course
          ERData.course = courseId

          # Unsubscribe the old course
          
          # Subscribe the new course
          sub = { type: "course", courseId: courseId }
          console.log("subscribing " + JSON.stringify(sub))
          Impressory.EventRoom.subscribe(sub)
          
          # Load the last few events
          $http.get("/course/" + courseId + "/chat/lastFew").success((data) -> 
            users = (event.addedBy for event in data when event.addedBy?)
            UserService.request(users)

            ERData.events = data
          )
          
          # TODO: this needs to go into a callback 
          ERData.events = []
      
      subscribe: (sub) ->
        Impressory.EventRoom.connect()
        Impressory.EventRoom.subscribe(sub)

      unsubscribe: (sub) ->
        Impressory.EventRoom.connect()
        Impressory.EventRoom.unsubscribe(sub)
      
      # Calculates the URL for a partial template to render this kind of event
      eventPartialUrl: (kind) -> "/eventPartial?kind=#{kind}"
    }
  ])


)