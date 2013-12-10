define(["./base"], (l) -> 

  Impressory.Controllers.Course.Create = ["$scope", "$http", "$location", ($scope, $http, $location) ->
  
    $scope.course = {}
    
    $scope.save = (course) -> 
      
      $scope.errors = [ ]
      
      $http.post('/courses/create', course).success((data) -> 
        $location.path("/course/" + data.id)
      ).error((data) ->
        $scope.errors = [ data.error || "Unexpected error" ]
        console.log(data)
      )    
  ]

)