define(["./base"], (l) -> 

  Impressory.Controllers.Course.Index = ["$scope", "$routeParams", "$http", ($scope, $routeParams, $http) ->
  
    $scope.courseId = $routeParams.courseId
  
    $scope.entries = $http.get("/course/" + $scope.courseId + "/allEntries").then((res) -> res.data.entries)
  ]

)