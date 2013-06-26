define(["./base"], (l) -> 

  Impressory.Controllers.Course.ActivityStream = ["$scope", "ContentService", "course", ($scope, ContentService, course) ->
  
    $scope.course = course
    
    $scope.entries = ContentService.activity(course.id)
    
    $scope.viewPath = (entry) -> ContentService.viewPath(entry)
  
  ]
  
  Impressory.Controllers.Course.ActivityStream.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
  }

)