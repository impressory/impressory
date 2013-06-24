define(["./base"], (l) -> 

  Impressory.Controllers.Course.Cover = ["$scope", "CourseService", "course", ($scope, CourseService, course) ->
  
    $scope.course = course
    
    $scope.courseId = course.id
  
    # Uses an invite code to register with this course
    $scope.useInvite = (codePacket) ->
      CourseService.useInvite(course.id, codePacket).then((data) -> $scope.course = data) 
  ]
  
  Impressory.Controllers.Course.Cover.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
  }

)