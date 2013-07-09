define(["./base"], (l) -> 

  Impressory.angularApp.directive("subheaderCourse", () -> 
    {
      restrict: 'E'
      scope: { course: '=course' }
      templateUrl: "directive_subheaderCourse.html"
    }
  )

)