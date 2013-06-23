define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.TopNav = ["$scope", "ContentService", ($scope, ContentService) ->
      
    $scope.voteUp = () -> ContentService.voteUp($scope.courseId, Impressory.Model.Viewing.Content.display?.id)
    
    $scope.voteDown = () -> ContentService.voteDown($scope.courseId, Impressory.Model.Viewing.Content.display?.id)
  ]

)