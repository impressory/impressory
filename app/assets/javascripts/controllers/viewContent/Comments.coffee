define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Comments = ["$scope", "$http", "viewingContent", "viewingUsers", "ContentService", ($scope, $http, viewingContent, viewingUsers, ContentService) ->
  
    $scope.newComment = {}
    
    $scope.users = Impressory.Model.Viewing.Users

    $scope.displayedEntry = Impressory.Model.Viewing.Content.display
  
    $scope.$watch("displayedEntry", (nv, ov) -> 
      users = (comment.addedBy for comment in $scope.displayedEntry.comments)
      viewingUsers.request(users)
    )
  
    $scope.addComment = (comment) ->
      displayed = Impressory.Model.Viewing.Content.display
      entryId = displayed?.id
      courseId = displayed?.course
      ContentService.addComment(courseId, entryId, comment.text)

  ]

)