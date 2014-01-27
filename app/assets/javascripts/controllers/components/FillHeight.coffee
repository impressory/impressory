define(["./base"], (l) -> 

  Impressory.Controllers.Components.FillHeight = ["$scope", "$rootScope", "$element", "$window", "DimensionsService", ($scope, $rootScope, $element, $window, DimensionsService) ->
    
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
    
    $scope.window = $window
    $scope.element = $element
    
  ]

  Impressory.angularApp.directive("imFillHeight", () -> 
    {
      restrict: 'A'
      controller: Impressory.Controllers.Components.FillHeight
    }
  )
  
)