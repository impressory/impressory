define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Sequence = ["$scope", "ContentService", "$location", ($scope, ContentService, $location) ->
  
    $scope.seqIndex = Impressory.Model.Viewing.Content.goToSeqIndex ? -1
    
    $scope.entries = []
    
    updateSeqEntry = () ->
      if $scope.seqIndex < 0
        Impressory.Model.Viewing.Content.seqEntry = null
      else 
        Impressory.Model.Viewing.Content.seqEntry = $scope.entries[$scope.seqIndex]
      
      # Update the entry that shows in the browser's location bar
      entry = Impressory.Model.Viewing.Content.seqEntry || $scope.entry
      path = ContentService.viewPath(entry)
      console.log("changing path to " + path)
      #$location.url(path)

    updateEntries = () -> 
      ContentService.request($scope.entry.course, $scope.entry.item.entries).then((entries) ->
        $scope.entries = entries
        updateSeqEntry()
      )
        
    $scope.$watch('entry.item.entries', () -> updateEntries())
      
    $scope.goToPrevEntry = () -> 
      $scope.seqIndex = $scope.seqIndex - 1
      updateSeqEntry()
    
    $scope.goToNextEntry = () -> 
      $scope.seqIndex = $scope.seqIndex + 1
      updateSeqEntry()
    
    $scope.goToStart = () -> 
      $scope.seqIndex = -1
      updateSeqEntry()
    
    $scope.hasNextEntry = () -> 
      $scope.seqIndex + 1 < $scope.entry?.item?.entries?.length
  
    $scope.hasPrevEntry = () -> 
      $scope.seqIndex >= 0
      
    # The first time the view appears, we'll recalculate the location but we want it
    # to replace the current entry in the history. (Subsequent changes will be appended)
    $location.replace()
  ]

)