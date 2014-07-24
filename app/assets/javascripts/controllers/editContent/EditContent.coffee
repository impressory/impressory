define(["./base"], (l) -> 

  Impressory.Controllers.EditContent.EditContent = ["$scope", "ContentService", ($scope, ContentService) ->
  
    unedited = $scope.entry
  
    $scope.entry = angular.copy($scope.entry)

    $scope.setPublished = () ->
      if !($scope.entry.settings.published?)
        $scope.entry.settings.published = (new Date).getTime();
  
    $scope.save = () ->
      $scope.errors = []
    
      ContentService.editItem($scope.entry).then(
        (entry) ->
          # Update the parent's scope entry that we were asked to edit 
          angular.copy(entry, unedited)
          
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