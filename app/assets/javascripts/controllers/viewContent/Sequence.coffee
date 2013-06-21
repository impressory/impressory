define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Sequence = ["$scope", "$http", "viewingContent", ($scope, $http, viewingContent) ->
  
    # Viewing.Content will already have been set up by TopNav
    $scope.content = Impressory.Model.Viewing.Content

    $scope.entries = Impressory.Model.Viewing.Content.entry?.item?.entries
    
    $scope.append = (entryId) -> 
      $http.post("/course/" + $scope.courseId + "/entry/" + Impressory.Model.Viewing.Content.entry.id + "/editItem", { append: entryId }).success((data) ->
        if data.entry?
          $scope.panels.toggleEditContent()
          viewingContent.updateEntryInPlace(data.entry)
        else 
          $scope.errors = [ "Unexpected result returned from server" ]
      ).error((data) -> 
        $scope.errors = [ data.error || "Unexpected error" ]
        console.log(data)
      )
    
  ]

)