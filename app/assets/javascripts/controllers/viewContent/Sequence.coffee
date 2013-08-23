define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Sequence = ["$scope", "$http", "viewingContent", ($scope, $http, viewingContent) ->
  
    # Viewing.Content will already have been set up by TopNav
    $scope.content = Impressory.Model.Viewing.Content
    
    $scope.goToPrevEntry = () -> viewingContent.goToPrevEntry()
    
    $scope.goToNextEntry = () -> viewingContent.goToNextEntry()
    
    $scope.goToStart = () -> viewingContent.goToStart()
    
    $scope.hasNextEntry = () -> viewingContent.hasNextEntry()
  
    $scope.hasPrevEntry = () -> viewingContent.hasPrevEntry()
  ]

)