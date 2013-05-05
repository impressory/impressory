define(["./base"], (l) -> 

  Impressory.Controllers.Course.Cover = ["$scope", "$routeParams", "viewingCourse", ($scope, $routeParams, viewingCourse) ->
  
    $scope.course = viewingCourse.get($routeParams.courseId)
  ]

)