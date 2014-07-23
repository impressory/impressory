define(["./base"], (l) -> 

  Impressory.Controllers.QnA.List = ["$scope", "QnAService", "viewingUsers", ($scope, QnAService, viewingUsers) ->
  
    $scope.users = Impressory.Model.Viewing.Users
    
    updateQuestions = () ->
      QnAService.fetchQuestions($scope.course.id)
        .success((data) ->
          $scope.questions = data.questions
          viewingUsers.request(q.addedBy for q in data.questions)
        )
      
    updateQuestions()
  ]

)