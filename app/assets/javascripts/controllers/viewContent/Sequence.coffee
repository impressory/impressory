define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Sequence = ["$scope", "viewingContent", ($scope, viewingContent) ->
      
    $scope.goToPrevEntry = () -> viewingContent.goToPrevEntry()
    
    $scope.goToNextEntry = () -> viewingContent.goToNextEntry()
    
    $scope.goToStart = () -> viewingContent.goToStart()
    
    $scope.hasNextEntry = () -> viewingContent.hasNextEntry()
  
    $scope.hasPrevEntry = () -> viewingContent.hasPrevEntry()
  ]

)