define(["./base"], (l) -> 

  Impressory.Controllers.MCPoll.View = ["$scope", "MCPollService", "markdownService", ($scope, MCPollService, markdownService) ->
  
    updateHtml = () -> 
      $scope.madeHtml = markdownService.makeHtml($scope.entry?.item?.text)
    
    updateHtml() 
    
    # Cause the user's pre-saved vote to load into the cache if it's not there
    MCPollService.getVote($scope.entry.id)

    # The values that the checkboxes will update
    $scope.selection = (false for option in $scope.entry.item.options)
    
    $scope.oldVote = () -> MCPollService.cache().get($scope.entry.id)
    
    # When the old vote loads (or re-loads if another instance of the poll saves a new vote),
    # reset the selection to the old vote
    $scope.$watch("oldVote()", (vote) ->
      $scope.selection = (false for option in $scope.entry.item.options)
      for index in vote.answer
        do (index) -> $scope.selection[index] = true
    )

    # Whether we're showing options or results
    $scope.showing = "options"
  
    $scope.show = (str) -> $scope.showing = "str"
    
    $scope.vote = () ->
      vote = (i for selected, i in $scope.selection when selected)
      MCPollService.vote($scope.entry.id, vote)
    
    $scope.selectedCount = () -> (i for selected, i in $scope.selection when selected).length
    
    # Whether the user has selected the maximum number of entries
    $scope.selectedMax = () -> $scope.selectedCount() >= $scope.entry.item.pick
    
    $scope.pushToStream = () -> MCPollService.pushToStream($scope.entry.id)
  
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