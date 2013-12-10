define(["./base"], (l) -> 

  Impressory.Controllers.Front.ListedCourses = ['$scope', 'CourseService', ($scope, CourseService) ->
  
    CourseService.listed().then((data) -> 
      $scope.courses = data
    ) 
  
  ]
)