define(["./base"], (l) -> 

  Impressory.Controllers.Components.SceTrustSrc = ["$scope", "$sce", ($scope, $sce) ->
    
    $scope.trustAsResourceUrl = (url) -> $sce.trustAsResourceUrl(url) 
    
  ]

)