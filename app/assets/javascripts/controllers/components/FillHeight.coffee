define(["./base"], (l) -> 

  Impressory.Controllers.Components.FillHeight = ["$scope", "$rootScope", "$element", "$window", "viewingContent", ($scope, $rootScope, $element, $window, viewingContent) ->
    
    $scope.refreshHeight = (nv) -> 
      $scope.fillHeight = $window.innerHeight - $element.offset().top

    $scope.$watch(
      () -> $element.offset().top
      (nv, ov) -> $scope.refreshHeight(nv)
    )
        
    $rootScope.$watch(
      () -> $window.innerHeight 
      (nv, ov) -> $scope.refreshHeight(nv)
    )    

    $scope.refreshHeight()
    
  ]

  Impressory.angularApp.directive("imFillHeight", () -> 
    {
      restrict: 'A'
      controller: Impressory.Controllers.Components.FillHeight
    }
  )

)