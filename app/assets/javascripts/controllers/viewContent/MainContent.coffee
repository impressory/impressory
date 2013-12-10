define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.MainContent = ["$scope", "ContentService", ($scope, ContentService) ->

    $scope.viewerPartialUrl = ContentService.viewerPartialUrl($scope.entry?.kind)

    $scope.activityStreamPartialUrl = ContentService.streamPartialUrl($scope.entry?.kind)
  ]


  Impressory.angularApp.directive("ceRenderEntryFull", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.MainContent
      scope: { entry: '=entry', seqIndex: '=', viewMode: '@' }
      templateUrl: "directive_ce_render_entry_full.html" 
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