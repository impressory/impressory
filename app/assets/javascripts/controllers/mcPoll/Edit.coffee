define(["./base"], (l) -> 

  Impressory.Controllers.MCPoll.Edit = ["$scope", "markdownService", ($scope,markdownService) ->
  
    $scope.$watch('entry.item.text', (newVal, oldVal) -> 
      $scope.preview = markdownService.makeHtml(newVal)
    )
    
    $scope.remove = (rmOption) ->
      $scope.entry.item.options = (option for option in $scope.entry.item.options when option isnt rmOption) 
      
    $scope.addOption = () ->
      $scope.entry.item.options.push({ option: "", score: 0 })
    
  ]

)