define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.ListContentForTopic = ["$scope", "ContentService", "UserService", ($scope, ContentService, UserService) ->

    $scope.entries = []
    
    $scope.userCache = UserService.userCache()
    
    runSearch = (topic) -> 
      ContentService.entriesForTopic($scope.course.id, topic).then((entries) -> 
        # Look up any users we haven't cached 
        users = (entry.addedBy for entry in entries)
        UserService.request(users)

        $scope.entries = entries
      )

    $scope.$watch('topic', (newVal, oldVal) -> runSearch(newVal))
  ]

  Impressory.angularApp.directive("ceContentForTopic", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.ListContentForTopic
      scope: { topic: '=topic', course: "=course" }
      templateUrl: "directive_ce_contentForTopic.html" 
    }
  )  
)