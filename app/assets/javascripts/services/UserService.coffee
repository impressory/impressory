define(["modules/base"], () ->

  Impressory.Services.UserService = Impressory.angularApp.service('UserService', ['$http', '$cacheFactory', '$q', ($http, $cacheFactory, $q) ->
   
    # Where we cache promises to avoid double-fetching
    cache = $cacheFactory('userServiceCache')
    
    # Where we cache returned items
    unwrappedCache = $cacheFactory('userServiceUnwrappedCache')
    
    Impressory.Caches["userServiceCache"] = cache
    
    Impressory.Caches["users"] = unwrappedCache
   
    {
      # return the unwrapped cache for immediate lookups from within templates
      userCache: () -> unwrappedCache
      
      # Returns a promise containing the JSON of a content entry
      get: (userId) ->
        cache.get(userId) || (
          promise =  $http.get("/users/#{userId}").then((res) -> 
            unwrappedCache.put(userId, res.data)
            res.data
          )
          cache.put(userId, promise)
          promise
        )
      
      # Fetches a list of users.  This performs a bulk fetch of users that have
      # not already been cached. The cache is immediately filled with individual
      # promises that will be fulfilled when the bulk fetch is complete. This 
      # allows the method to work both to pre-cache users that will then be 
      # individually requested (eg, by a sub-template or directive), and to 
      # fetch a promise for the list of users.
      request: (ids) ->
      
        # Eliminate the ids we have already fetched
        unfetched = (x for x in ids when !(cache.get(x)?))
        
        # If there are any left to request, request them
        if unfetched.length > 0
          
          deferred = []
          
          # put a new promise into the cache, because these are being fetched
          for unfetchedId in unfetched
            do (unfetchedId) -> 
              deferred[unfetchedId] = $q.defer()
              cache.put(unfetchedId, deferred[unfetchedId].promise)
            
          # fetch them and fulfill the promises
          $http.post("/users/findByIds", { ids: unfetched }).then((res) ->
            for user in res.data 
              do (user) ->
                unwrappedCache.put(user.id, user)
                deferred[user.id]?.resolve(user)
          )
          
        # We now have promises for every id
        promises = (cache.get(id) for id in ids)
        
        # Combine them all as a single promise to return
        $q.all(promises)
      
      
      # Change password
      changePassword: (oldPassword, newPassword) ->
        $http.post("/self/changePassword", { oldPassword: oldPassword, newPassword: newPassword }).then(
          (res) ->
            # update the record of self
            Impressory.Model.Login.login(res.data) 
            res.data
          ,
          (res) -> $q.reject(res.data.error || "Unexpected error")
        )
    }
  ])


)