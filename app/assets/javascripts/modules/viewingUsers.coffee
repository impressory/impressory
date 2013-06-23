define(["./app"], () ->

  Impressory.angularApp.service('viewingUsers', ['$http', '$location', ($http, $location) ->
      
    viewing = Impressory.Model.Viewing
    
    {
      request: (ids) ->
        $http.post("/users/findByIds", { ids: ids }).then((res) ->
          for user in res.data
            viewing.Users.cache[user.id] = user
        )
    }
  ])

)