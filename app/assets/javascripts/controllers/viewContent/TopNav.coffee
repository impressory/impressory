define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.TopNav = ["$scope", ($scope) ->
    
    $scope.downVote = () -> console.log("Voting down")
    
    $scope.upVote = () -> console.log("Voting up")
  ]

)