define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.ViewContent = ["$scope", "$routeParams", "$location", "viewingCourse", ($scope, $routeParams, $location, viewingCourse) ->


    updateView = (path) ->
      $scope.entryQuery = {
        courseId: $routeParams.courseId
        entryId: $routeParams.entryId
        adj: $routeParams.adj
        noun: $routeParams.noun
        topic: $routeParams.topic
        site: $routeParams.site
      }
      
      viewingCourse.queryContent($scope.entryQuery)
      
    $scope.$watch('$location.path()', updateView)
    
    $scope.viewing = Impressory.Model.Viewing
    
    # Used by ng-click in slide-sorter, amongst others
    $scope.lookUp = (params) -> $location.search(params)
        
  
  ]

)