define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.MainContent = ["$scope", "ContentService", ($scope, ContentService) ->

    $scope.viewerPartialUrl = ContentService.viewerPartialUrl($scope.entry.kind)

    $scope.activityStreamPartialUrl = ContentService.streamPartialUrl($scope.entry.kind)
  ]


  Impressory.angularApp.directive("ceRenderEntryFull", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.MainContent
      scope: { entry: '=entry', viewMode: '@' }
      template: "<div class='content-container'><div ng-include='viewerPartialUrl'></div></div>" 
    }
  )  

  Impressory.angularApp.directive("ceRenderEntryStream", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.MainContent
      scope: { entry: '=entry', viewMode: '@' }
      template: """
        <div ng-include='activityStreamPartialUrl'></div>  
      """ 
    }
  )  
)