define(["./base"], (l) -> 

  Impressory.Controllers.Course.ActivityStream = ["$scope", "ContentService", "viewingEvents", "course", ($scope, ContentService, viewingEvents, course) ->
  
    $scope.course = course
    
    $scope.login = Impressory.Model.Login
    
    # The parent scope will have set the value of courseId
    viewingEvents.showForCourse(course.id)    
    
    $scope.refreshList = () -> 
      $scope.publishedSinceRefresh = 0
      ContentService.activity(course.id).then((entries) -> 
        $scope.entries = entries
      ) 
    	
    $scope.refreshList()
    
    $scope.$on("push", 
      (event, msg) ->
        if msg.type == "content entry published"
          entry = msg.entry
          if entry.settings.inNews
            ContentService.get(entry.course, entry.id).then((got) -> $scope.entries.push(got))
            $scope.$apply()
    )


  ]
  
  Impressory.Controllers.Course.ActivityStream.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
  }



)