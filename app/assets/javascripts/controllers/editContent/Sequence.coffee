define(["./base"], (l) -> 

  Impressory.Controllers.EditContent.Sequence = ["$scope", "ContentService", ($scope, ContentService) ->
  
    $scope.append = (entryId) ->       
      ContentService.get($scope.entry.course, entryId).then((e) ->
        $scope.entry.item.entries.push(e)
      )
    
    
    $scope.moveFrom = (index) ->
      if $scope.movingFrom == index
        $scope.moving = false
        $scope.movingFrom = null
      else 
        $scope.movingFrom = index
        $scope.moving = true
    
    $scope.moveTo = (index) ->
      if $scope.movingFrom?
        item = $scope.entry.item.entries.splice($scope.movingFrom, 1)        
        if index > $scope.movingFrom then index = index - 1        
        $scope.entry.item.entries.splice(index, 0, item[0])
        $scope.moving = false
        $scope.movingFrom = null

    $scope.remove = (index) -> 
        $scope.entry.item.entries.splice($scope.movingFrom, 1)
    
    $scope.moving = false
    
    $scope.movingFrom = null
  ]

)