define(["./base"], (l) -> 

  Impressory.Controllers.EditContent.Sequence = ["$scope", "ContentService", ($scope, ContentService) ->
  
    $scope.append = (entryId) ->       
      ContentService.get($scope.entry.course, entryId).then((e) ->
        $scope.entry.item.entries.push(e)
      )
    
  ]

)