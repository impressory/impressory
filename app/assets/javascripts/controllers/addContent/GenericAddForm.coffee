define(["./base"], (l) ->

  Impressory.Controllers.AddContent.GenericAddForm = ["$scope", "ContentService", "viewingContent", ($scope, ContentService, viewingContent) ->
  
    $scope.errors = [ ]
  
    $scope.toAdd = { 
      entry: {
        tags: {
          adjectives: []
          nouns: []
          topics: Impressory.Model.Viewing.Content.display?.tags.topics || [ "nothing" ]
        }
        item: {}
      }      
    }
    
    # So that subcomponents looking for entry will work
    $scope.entry = $scope.toAdd.entry
    
    $scope.item = $scope.toAdd.entry.item
  
    $scope.submit = (kind) ->
      $scope.toAdd.entry.kind = kind
      ContentService.addContent($scope.courseId, $scope.toAdd.entry ).success((data) -> 
        viewingContent.viewThisData(data)
      ).error((data) ->
        if (data.error?)
          $scope.errors = [ data.error ]
        else
         $scope.errors = [ "Unexpected error" ]
         console.log(data)
      )      
  
  ]

)