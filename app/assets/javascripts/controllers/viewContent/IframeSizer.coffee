define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.IframeSizer = ["$scope", "$rootScope", "$element", "$window", ($scope, $rootScope, $element, $window) ->
    
    $rootScope.$watch('wh', (nv, ov) -> 
      $scope.fillHeight = nv - $element.offset().top - 4
    )
      
  ]

)