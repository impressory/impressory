define(["./base"], (l) -> 

  Impressory.Controllers.Front.MyCourses = ['$scope', 'CourseService', ($scope, CourseService) ->
  
    $scope.login = Impressory.Model.Login
    
    $scope.$watch("login", () -> 
      $scope.updateCourses()
    , true)
    
    $scope.updateCourses = () -> 
      CourseService.my().then((courses) -> 
        $scope.courses = courses
      ) 
  
  ]
)