define(["./base"], (l) -> 

  Impressory.Controllers.MarkdownPage.Edit = ["$scope", "markdownService", ($scope,markdownService) ->
  
    updatePreview = () -> 
      $scope.preview = markdownService.makeHtml($scope.entry?.item?.text)
    
    $scope.showEdit = () -> $scope.tab = "text"
    
    $scope.showPreview = () ->
      updatePreview() 
      $scope.tab = "preview"
      
    $scope.showAddIns = () -> 
      $scope.tab = "addIns"
      
    $scope.showEdit()
  
  ]

)