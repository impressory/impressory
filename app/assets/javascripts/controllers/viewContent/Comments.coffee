define(["./base"], (l) -> 

  # This assumes the parent scope has set $scope.entry
  Impressory.Controllers.ViewContent.Comments = ["$scope", "$http", "viewingContent", "viewingUsers", "ContentService", ($scope, $http, viewingContent, viewingUsers, ContentService) ->
  
    $scope.newComment = {}
    
    $scope.users = Impressory.Model.Viewing.Users

    $scope.$watch("entry", (nv, ov) -> 
      users = (comment.addedBy for comment in $scope.entry?.comments || [])
      viewingUsers.request(users)
    )
  
    $scope.addComment = (comment) -> ContentService.addComment($scope.entry, comment.text)

  ]

)