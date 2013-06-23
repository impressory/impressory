define(["./app"], () ->

  Impressory.angularApp.service('viewingUsers', ['$http', '$location', ($http, $location) ->
      
    viewing = Impressory.Model.Viewing
    
    {
      request: (ids) ->
        # Eliminate the ids we have already fetched
        unfetched = (x for x in ids when !(viewing.Users.cache[x]?))
        
        # If there are any left to request, request them
        if unfetched.length > 0
          $http.post("/users/findByIds", { ids: unfetched }).then((res) ->
            for user in res.data
              viewing.Users.cache[user.id] = user
          )
    }
  ])

)