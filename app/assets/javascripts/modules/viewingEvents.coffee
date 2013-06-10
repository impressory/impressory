define(["./app"], () ->

  Impressory.angularApp.service('viewingEvents', ['$http', '$location', '$rootScope', 'viewingUsers', ($http, $location, $rootScope, viewingUsers) ->
    
    ERData = Impressory.Model.Viewing.EventRoom

    Impressory.EventRoom = EventRoom.create()
    
    Impressory.EventRoom.addListener({ 
      receive: (msg) -> 
        console.log("Received a message")
        switch msg.kind
          when "push"
            ERData.events.push(msg)
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
            if data.events?
              users = (event.addedBy for event in data.events)
              viewingUsers.request(users)

              ERData.events = data.events
          )
          
          # TODO: this needs to go into a callback 
          ERData.events = []
      
      subscribe: (sub) ->
        Impressory.EventRoom.connect()
        Impressory.EventRoom.subscribe(sub)

      unsubscribe: (sub) ->
        Impressory.EventRoom.connect()
        Impressory.EventRoom.unsubscribe(sub)
    }
  ])


)