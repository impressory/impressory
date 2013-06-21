define(["./base"], (l) -> 

  Impressory.Controllers.EditContent.EditContent = ["$scope", "$http", "viewingContent", ($scope, $http, viewingContent) ->
  
    $scope.entry = angular.copy(Impressory.Model.Viewing.Content.display)
  
    $scope.save = () ->
      $scope.errors = []
    
      $http.post("/course/" + $scope.courseId + "/entry/" + $scope.entry.id + "/editItem", $scope.entry).success((data) ->
        if data.entry?
          $scope.panels.toggleEditContent()
          viewingContent.updateEntryInPlace(data.entry)
        else 
          $scope.errors = [ "Unexpected result returned from server" ]
      ).error((data) -> 
        $scope.errors = [ data.error || "Unexpected error" ]
      )
  
  ]

)