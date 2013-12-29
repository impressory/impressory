define(["./base"], (l) -> 

  # This assumes the parent scope has set $scope.entry
  Impressory.Controllers.ViewContent.Comments = ["$scope", "$http", "viewingContent", "UserService", "ContentService", ($scope, $http, viewingContent, UserService, ContentService) ->
  
    $scope.newComment = {}
    
    $scope.userCache = UserService.userCache()

    $scope.$watch("entry", (nv, ov) -> 
      users = (comment.addedBy for comment in $scope.entry?.comments || [])
      UserService.request(users)
    )
  
    $scope.addComment = (comment) -> ContentService.addComment($scope.entry, comment.text)

  ]

)