define(["./base"], (l) -> 

  Impressory.Controllers.QnA.Create = ["$scope", "$location", "QnAService", "markdownService", ($scope, $location, QnAService, markdownService) ->
  
    $scope.question = {}
  
    $scope.$watch('question.text', (nv, ov) -> 
      $scope.question.madeHtml = markdownService.makeHtml($scope.question?.text || "")
    )

    $scope.save = (question) ->
      QnAService.createQuestion($scope.course.id, question)
        .success((question) -> $location.path("/course/#{$scope.course.id}/qna/#{question.id}"))
        .error((data) ->
          $scope.errors = [ data.error || "Unexpected error" ]
          console.log(data)
        )
  ]

)