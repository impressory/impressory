define(["./base"], (l) ->

  Impressory.Controllers.AddContent.GenericAddForm = ["$scope", "$http", "viewingCourse", ($scope, $http, viewingCourse) ->
  
    $scope.errors = [ ]
  
    $scope.toAdd = { }
  
    $scope.submit = (kind) ->
      $scope.toAdd["kind"] = kind
      $http.post("addContent", $scope.toAdd ).success((data) -> 
        if (data.error?)
          $scope.errors = [ data.error ]
        else
          Impressory.Model.Viewing.Content = data
          console.log("Updating data on successful post")
          viewingCourse.updateLocation(false)
      
      ).error((data) ->
         $scope.errors = [ "Unexpected error" ]
         console.log(data)
      )
      
  
  ]

)