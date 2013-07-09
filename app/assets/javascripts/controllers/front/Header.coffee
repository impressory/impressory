define(["./base"], (l) -> 

  Impressory.angularApp.directive("headerFront", () -> 
    {
      restrict: 'E'
      templateUrl: "directive_headerFront.html"
    }
  )

)