define(["./base"], () ->

  Impressory.angularApp.service('MCPollService', ['$http', '$cacheFactory', 'ContentService', '$q', ($http, $cacheFactory, ContentService, $q) ->
   
    voteCache = $cacheFactory("mcPollVoteCache")
    
    unwrappedCache = $cacheFactory("mcPollUnwrappedVoteCache")
   
    {
      cache: () -> unwrappedCache
    
      vote: (pollId, items) -> 
        $http.post("/poll/multipleChoice/#{pollId}/vote", { options: items }).then((res) ->
          vote = res.data
          
          unwrappedCache.put(pollId, vote)
          
          defer = $q.defer()
          voteCache.put(pollId, defer.promise)
          defer.resolve(vote)
          
          vote
        )
      
      getVote: (pollId) ->
        voteCache.get(pollId) || ( 
          prom = $http.get("/poll/multipleChoice/#{pollId}/vote").then((res) -> 
            vote = res.data
            unwrappedCache.put(pollId, vote)
            vote
          )
          voteCache.put(pollId, prom)
          prom
        )
       
      pushToStream: (pollId) ->
        $http.post("/poll/multipleChoice/#{pollId}/push", { })
    }
  ])


)