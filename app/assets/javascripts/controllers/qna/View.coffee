define(["./base"], (l) -> 

  Impressory.Controllers.QnA.View = ["$scope", "$http", "$routeParams", "markdownService", "viewingUsers", ($scope, $http, $routeParams, markdownService, viewingUsers) ->
  
    $scope.users = Impressory.Model.Viewing.Users
  
    $scope.questionId = $routeParams.questionId

    requestUsers = () -> 
      usersToReq = {}
      usersToReq[$scope.question.addedBy] = true
      
      for comment in $scope.question.comments
        usersToReq[comment.addedBy] = true
      for answer in $scope.question.answers
        usersToReq[answer.addedBy] = true
        for comment in answer.comments
          usersToReq[comment.addedBy] = true
      
      usersArr = []
      for user, bool of usersToReq
        usersArr.push(user)

      viewingUsers.request(usersArr)
    
    
    $scope.updateQuestion = (data) -> 
      $scope.question = data
      $scope.question.madeHtml = markdownService.makeHtml(data.text)
      for answer in $scope.question.answers
        answer.madeHtml = markdownService.makeHtml(answer.text)
        
      requestUsers()
      
    
    reloadQuestion = () -> 
      $http.get($scope.questionId).success((data) -> 
        $scope.updateQuestion(data)
      ).error((res) -> 
        $scope.errors = [ "Unexpected error" ]
      )
    
    reloadQuestion()
    
  ]
  
  
  Impressory.Controllers.QnA.NewAnswer = ["$scope", "$http", "markdownService", ($scope, $http, markdownService) -> 
  
    $scope.answer = { text: "" }
  
    $scope.$watch('answer.text', (nv, ov) -> 
      $scope.answer.madeHtml = markdownService.makeHtml($scope.answer.text)
    )
    
    $scope.save = (answer) -> 
      $http.post('/course/' + $scope.courseId + '/qna/' + $scope.questionId + '/newAnswer', answer).success((data) -> 
        if (data.question?)
          $scope.updateQuestion(data.question)
        if (data.error?)
          $scope.errors = [ data.error ]
        else
          console.log(data)
      ).error((data) ->
        $scope.errors = [ "Unexpected error" ]
        console.log(data)
      )
  
  ]
  
  Impressory.Controllers.QnA.NewQComment = ["$scope", "$http", ($scope, $http) -> 
  
    $scope.comment = { text: "" }
  
    $scope.save = (comment) -> 
      $http.post('/course/' + $scope.courseId + '/qna/' + $scope.questionId + '/newComment', comment).success((data) -> 
        if (data.question?)
          $scope.updateQuestion(data.question)
        if (data.error?)
          $scope.errors = [ data.error ]
        else
          console.log(data)
      ).error((data) ->
        $scope.errors = [ "Unexpected error" ]
        console.log(data)
      )    
  
  ]
  
  Impressory.Controllers.QnA.NewAnsComment = ["$scope", "$http", ($scope, $http) -> 
  
    $scope.comment = { text: "" }
  
    $scope.save = (answerId, comment) -> 
      $http.post('/course/' + $scope.courseId + '/qna/' + $scope.questionId + '/answer/' + answerId + '/newComment', comment).success((data) -> 
        if (data.question?)
          $scope.updateQuestion(data.question)
        if (data.error?)
          $scope.errors = [ data.error ]
        else
          console.log(data)
      ).error((data) ->
        $scope.errors = [ "Unexpected error" ]
        console.log(data)
      )    
  
  ]  

)