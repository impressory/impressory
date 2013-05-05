define(["./base"], (l) -> 

  Impressory.Controllers.Course.Create = ["$scope", "$http", "$location", ($scope, $http, $location) ->
  
    $scope.course = {}
    
    $scope.save = (course) -> 
      
      $scope.errors = [ ]
      
      $http.post('/courses/create', course)
       .success((data) -> 
         if (data.course?)
           $location.path("/course/" + data.course.id)
         if (data.error?)
           $scope.errors = [ data.error ]
         else
           console.log(data)
       )
       .error((data) ->
         $scope.errors = [ "Unexpected error" ]
         console.log(data)
       )    
  ]

)