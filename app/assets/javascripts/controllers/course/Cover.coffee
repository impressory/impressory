define(["./base"], (l) -> 

  Impressory.Controllers.Course.Cover = ["$scope", "$routeParams", "viewingCourse", ($scope, $routeParams, viewingCourse) ->
  
    $scope.courseId = $routeParams.courseId
  
    $scope.course = viewingCourse.get($scope.courseId)
  ]

)