define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Voting = ["$scope", "ContentService", ($scope, ContentService) ->
      
    $scope.voteUp = (entry) -> ContentService.voteUp(entry)
    
    $scope.voteDown = (entry) -> ContentService.voteDown(entry)
  ]
  
  Impressory.angularApp.directive("entryVote", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.Voting
      scope: { 
        entry: '=entry',
        viewMode: '@' 
      }
      templateUrl: "directive_ce_vote.html"
    }
  )
  

)