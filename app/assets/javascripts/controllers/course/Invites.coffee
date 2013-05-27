# Controller for the view that manages and creates invites

define(["./base"], (l) -> 

  Impressory.Controllers.Course.Invites = ["$scope", "$http", "$location", ($scope, $http, $location) ->
  
    fetchInvites = () ->
      $http.get("/course/" + $scope.courseId + "/invites").success((data) -> 
        if data.invites?
          $scope.invites = data.invites
        else if data.error?
          $scope.errors = [ data.error ]
        else
          $scope.errors = [ "Unexpected error" ]
      ).error((err) -> 
        console.log(err)
        $scope.errors = [ "Unexpected error: " + err ]
      ) 
  
    fetchInvites()
    
    $scope.create = (newInvite) ->
      $http.post("/course/" + $scope.courseId + "/createInvite", newInvite).success((data) ->
        fetchInvites()
      )
     
  ]

)