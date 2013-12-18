define(["./base"], (l) -> 

  Impressory.Controllers.EditContent.Sequence = ["$scope", "ContentService", ($scope, ContentService) ->
  
    updateEntries = () -> 
      ContentService.request($scope.entry.course, $scope.entry.item.entries).then((entries) ->
        $scope.entries = entries
      )
      
    updateEntries()
  
    $scope.append = (entryId) ->       
      $scope.entry.item.entries.push(entryId)
      updateEntries()
    
    $scope.moveFrom = (index) ->
      if $scope.movingFrom == index
        $scope.moving = false
        $scope.movingFrom = null
      else 
        $scope.movingFrom = index
        $scope.moving = true
      updateEntries()
    
    $scope.moveTo = (index) ->
      if $scope.movingFrom?
        item = $scope.entry.item.entries.splice($scope.movingFrom, 1)        
        if index > $scope.movingFrom then index = index - 1        
        $scope.entry.item.entries.splice(index, 0, item[0])
        $scope.moving = false
        $scope.movingFrom = null
      updateEntries()

    $scope.remove = (index) -> 
      $scope.entry.item.entries.splice($scope.movingFrom, 1)
      updateEntries()
    
    $scope.moving = false
    
    $scope.movingFrom = null
  ]

)