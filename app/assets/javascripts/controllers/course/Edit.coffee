define(["./base"], (l) -> 

  Impressory.Controllers.Course.Edit = ["$scope", "$location", "CourseService", "course", ($scope, $location, CourseService, course) ->
  
    $scope.course = course
    
    console.log(course)
  
    $scope.saveCourse = (course) -> 
      CourseService.save(course).then((course) -> 
        $location.path("/course/#{course.id}")
      )
    
  ]
  
  Impressory.Controllers.Course.Edit.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
  }
  
)