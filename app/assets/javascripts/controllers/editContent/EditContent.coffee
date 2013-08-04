define(["./base"], (l) -> 

  Impressory.Controllers.EditContent.EditContent = ["$scope", "ContentService", ($scope, ContentService) ->
  
    $scope.entry = angular.copy(Impressory.Model.Viewing.Content.display)
  
    $scope.save = () ->
      $scope.errors = []
    
      ContentService.editItem($scope.entry).then(
        (entry) -> 
          $scope.panels.toggleEditContent()
        (data) ->
          $scope.errors = [ data.error || "Unexpected error" ]
      )
  
  ]

)