define(["./base"], (l) -> 

  Impressory.Controllers.Course.Index = ["$scope", "$routeParams", "$http", "viewingUsers", ($scope, $routeParams, $http, viewingUsers) ->
  
    $scope.courseId = $routeParams.courseId
  
    $scope.entries = $http.get("/course/" + $scope.courseId + "/allEntries").then((res) -> 
      entries = res.data.entries 
        
      # Look up any users we haven't cached 
      users = (entry.addedBy for entry in entries)
      viewingUsers.request(users)
        
      entries
    )
    
  ]

)