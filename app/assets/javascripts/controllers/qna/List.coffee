define(["./base"], (l) -> 

  Impressory.Controllers.QnA.List = ["$scope", "$http", "viewingUsers", ($scope, $http, viewingUsers) ->
  
    $scope.users = Impressory.Model.Viewing.Users
    
    updateQuestions = () -> 
      $http.get('/course/' + $scope.courseId + '/qna').success((data) -> 
        $scope.questions = data.questions
        viewingUsers.request(q.addedBy for q in data.questions)
      )  
      
    updateQuestions()
  ]

)