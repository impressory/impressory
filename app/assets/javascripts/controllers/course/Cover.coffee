define(["./base"], (l) -> 

  Impressory.Controllers.Course.Cover = ["$scope", "$routeParams", "$http", "viewingCourse", ($scope, $routeParams, $http, viewingCourse) ->
  
    $scope.courseId = $routeParams.courseId
  
    $scope.course = viewingCourse.get($scope.courseId)
    
    # Uses an invite code to register with this course
    $scope.useInvite = (codePacket) -> 
      $http.post("/course/" + $scope.courseId + "/useInvite", codePacket).success((data) -> 
        # Force a refresh of the course information
        $scope.course = viewingCourse.fetchCourse($scope.courseId)
      )
  ]

)