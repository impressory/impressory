define(["./base"], (l) -> 

  Impressory.Controllers.MarkdownPage.View = ["$scope", "MarkdownService", ($scope,markdownService) ->
  
    updateHtml = () -> 
      $scope.madeHtml = markdownService.makeHtml($scope.entry?.item?.text)
    
    $scope.$watch('entry.item.text', () -> updateHtml())
  
  ]

)