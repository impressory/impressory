define(["./base"], (l) -> 

  Impressory.Controllers.Front.MyCourses = ['$scope', '$http', ($scope, $http) ->
  
    $scope.login = Impressory.Model.Login
    
    $scope.$watch("login", () -> 
      $scope.updateCourses()
    , true)
    
    $scope.updateCourses = () -> 
      $http.get("/courses/my").success((data) -> 
        $scope.courses = data.courses
      ) 
  
  ]
)