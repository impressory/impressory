define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.MainContent = ["$scope", "ContentService", ($scope, ContentService) ->

    $scope.viewerPartialUrl = ContentService.viewerPartialUrl($scope.entry.kind)

    $scope.activityStreamPartialUrl = ContentService.viewerPartialUrl($scope.entry.kind)
  ]

)