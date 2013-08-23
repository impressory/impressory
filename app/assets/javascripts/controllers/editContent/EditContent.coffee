define(["./base"], (l) -> 

  Impressory.Controllers.EditContent.EditContent = ["$scope", "ContentService", ($scope, ContentService) ->
  
    $scope.entry = angular.copy(Impressory.Model.Viewing.Content.display)
  
    $scope.save = () ->
      $scope.errors = []
    
      ContentService.editItem($scope.entry).then(
        (entry) -> 
          $scope.onClose()
        (data) ->
          $scope.errors = [ data.error || "Unexpected error" ]
      )
      
    $scope.editPartialUrl = ContentService.editPartialUrl($scope.entry.kind)
  
  ]

  Impressory.angularApp.directive("ceEditContentItem", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.EditContent.EditContent
      scope: { entry: '=entry', onClose: '&' }
      templateUrl: "directive_ce_edit_content.html" 
    }
  )  

)