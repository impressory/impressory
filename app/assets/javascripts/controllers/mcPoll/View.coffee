define(["./base"], (l) -> 

  Impressory.Controllers.MCPoll.View = ["$scope", "markdownService", ($scope,markdownService) ->
  
    updateHtml = () -> 
      $scope.madeHtml = markdownService.makeHtml($scope.entry?.item?.text)
    
    updateHtml() 
    
    $scope.showing = "options"
  
    $scope.show = (str) -> $scope.showing = "str"
  
  ]

)