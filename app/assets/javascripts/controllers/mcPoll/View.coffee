define(["./base"], (l) -> 

  Impressory.Controllers.MCPoll.View = ["$scope", "$http", "markdownService", ($scope, $http, markdownService) ->
  
    updateHtml = () -> 
      $scope.madeHtml = markdownService.makeHtml($scope.entry?.item?.text)
    
    updateHtml() 
    
    $scope.showing = "options"
  
    $scope.show = (str) -> $scope.showing = "str"
    
    $scope.vote = () ->
      items = (i for option, i in $scope.entry.item.options when option.selected)
      $http.post("/course/" + $scope.entry.course + "/entry/" + $scope.entry.id + "/mcPollVote", { options: items })
    
    $scope.recountSelected = () ->
      $scope.selected = (i for option, i in $scope.entry.item.options when option.selected) 
      
    $scope.recountSelected()
  
  ]

  Impressory.Controllers.MCPoll.Results = ["$scope", "viewingEvents", ($scope, viewingEvents) ->
  
    $scope.states = Impressory.Model.Viewing.EventRoom.states
    
    $scope.$on("Multiple choice poll results", (ngEvent, event) ->      
      if (event.id == $scope.entry.id) 
        for option in $scope.entry.item.options
          option.votes = 0
          option.percent = 0
      
        total = 0
        for pair in event.results
          total = total + pair.votes
          
        for pair in event.results
          $scope.entry.item.options[pair.option].votes = pair.votes
          $scope.entry.item.options[pair.option].percent = (pair.votes / total) * 100
           
        $scope.$apply()
      
    )
    
    viewingEvents.subscribe({ type: "Multiple choice poll results", id: $scope.entry.id})
    
    
  
  ]

)