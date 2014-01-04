define(["./base"], (l) -> 

  Impressory.Controllers.Course.Index = ["$scope", "course", "entries", ($scope, course, entries) ->
  
    $scope.courseId = course.id
  
    $scope.course = course
    $scope.entries = entries
    
  ]

  Impressory.Controllers.Course.Index.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
    entries: ['$route', 'ContentService', ($route, ContentService) -> 
      ContentService.allEntries($route.current.params.courseId)
    ] 
  }


)