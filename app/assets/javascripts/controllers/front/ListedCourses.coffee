define(["./base"], (l) -> 

  Impressory.Controllers.Front.ListedCourses = ['$scope', '$http', ($scope, $http) ->
  
    $http.get("/courses/listed").success((data) -> 
      $scope.courses = data.courses
    ) 
  
  ]
)