define(["./base"], (l) ->

  Impressory.Controllers.AddContent.GenericAddForm = ["$scope", "$http", "viewingContent", ($scope, $http, viewingContent) ->
  
    $scope.errors = [ ]
  
    $scope.toAdd = { 
      entry: {
        adjectives: []
        nouns: []
        topics: Impressory.Model.Viewing.Content.display?.topics || [ "nothing" ]
      }
      item: {}
    }
    
    # So that subcomponents looking for entry will work
    $scope.entry = $scope.toAdd.entry
    
    $scope.item = $scope.toAdd.item
  
    $scope.submit = (kind) ->
      $scope.toAdd["kind"] = kind
      $http.post("addContent", $scope.toAdd ).success((data) -> 
        if (data.error?)
          $scope.errors = [ data.error ]
        else
          viewingContent.viewThisData(data)
      ).error((data) ->
         $scope.errors = [ "Unexpected error" ]
         console.log(data)
      )
      
  
  ]

)