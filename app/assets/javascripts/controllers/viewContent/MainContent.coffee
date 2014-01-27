define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.MainContent = ["$scope", "ContentService", "viewingContent", ($scope, ContentService, viewingContent) ->

    $scope.viewerPartialUrl = ContentService.viewerPartialUrl($scope.entry?.kind)

    $scope.streamPartialUrl = ContentService.streamPartialUrl($scope.entry?.kind)
    
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
      scope: { entry: '=entry', expanded: '=' }
      templateUrl: "directive_ce_render_entry_stream.html" 
    }
  )  

  Impressory.Controllers.ViewContent.LoadEntry = ["$scope", "ContentService", ($scope, ContentService) ->
    ContentService.get($scope.courseId, $scope.entryId).then((entry) ->
      $scope.loaded = entry
      $scope.onLoad({ loaded: entry })
    )
  ]

  Impressory.angularApp.directive("ceLoadEntry", () -> 
    {
      restrict: 'AE'
      controller: Impressory.Controllers.ViewContent.LoadEntry
      scope: { courseId: '@courseId', entryId: '@entryId', onLoad: '&' }
      transclude: true
      template: "<div ng-transclude></div>"
    }
  )  
)