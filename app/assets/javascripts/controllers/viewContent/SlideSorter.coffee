define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.SlideSorter = ["$scope", ($scope) ->
  
    # Viewing.Content will already have been set up by TopNav
    $scope.content = Impressory.Model.Viewing.Content

  ]

)