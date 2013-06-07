define(["./base"], (l) -> 

  Impressory.Controllers.QnA.Create = ["$scope", "$http", "$location", "markdownService", ($scope, $http, $location, markdownService) ->
  
    $scope.$watch('question.text', (nv, ov) -> 
      $scope.question.madeHtml = markdownService.makeHtml($scope.question.text)
    )

    $scope.save = (question) -> 
      
      $http.post('/course/' + $scope.courseId + '/qna/new', question)
       .success((data) -> 
         if (data.question?)
           $location.path("/course/" + $scope.courseId + "/qna/" + data.question.id)
         if (data.error?)
           $scope.errors = [ data.error ]
         else
           console.log(data)
       )
       .error((data) ->
         $scope.errors = [ "Unexpected error" ]
         console.log(data)
       )
  ]

)