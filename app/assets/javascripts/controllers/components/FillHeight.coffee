define(["./base"], (l) -> 

  Impressory.Controllers.Components.FillHeight = ["$scope", "$rootScope", "$element", "$window", "viewingContent", ($scope, $rootScope, $element, $window, viewingContent) ->
    
    $rootScope.$watch('wh', (nv, ov) -> 
      $scope.refreshHeight(nv)
    )
    
    
    $scope.refreshHeight = (nv) -> 
      $scope.fillHeight = $window.innerHeight - $element.offset().top
    
    viewingContent.refreshHeight()
    $scope.refreshHeight()
    
  ]

)