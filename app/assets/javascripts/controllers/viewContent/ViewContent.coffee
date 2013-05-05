define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.ViewContent = ["$scope", "$routeParams", "viewingCourse", ($scope, $routeParams, viewingCourse) ->
  
    $scope.entryQuery = {
      courseId: $routeParams.courseId
      entryId: $routeParams.entryId
      adj: $routeParams.adj
      noun: $routeParams.noun
      topic: $routeParams.topic
      site: $routeParams.site
    }
    
    viewingCourse.queryContent($scope.entryQuery)
    
    $scope.viewing = Impressory.Model.Viewing
  
  ]

)