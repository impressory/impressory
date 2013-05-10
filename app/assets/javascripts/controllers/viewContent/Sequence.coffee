define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Sequence = ["$scope", ($scope) ->
  
    # Viewing.Content will already have been set up by TopNav
    $scope.content = Impressory.Model.Viewing.Content

    $scope.entries = Impressory.Model.Viewing.Content.entry?.item?.entries
    
    
  ]

)