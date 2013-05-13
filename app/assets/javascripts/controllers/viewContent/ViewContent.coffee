define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.ViewContent = ["$scope", "$routeParams", "$location", "viewingContent", ($scope, $routeParams, $location, viewingContent) ->

    updateView = (path) ->
      $scope.courseId = $routeParams.courseId

      $scope.entryQuery = {
        courseId: $routeParams.courseId
        entryId: $routeParams.entryId
        adj: $routeParams.adj
        noun: $routeParams.noun
        topic: $routeParams.topic
        site: $routeParams.site
      }
      
      viewingContent.lookUp($scope.entryQuery)
      
    updateView()
      
    $scope.$on('$routeUpdate', updateView)
    
    $scope.viewing = Impressory.Model.Viewing
    
    # Used by ng-click in slide-sorter, amongst others
    $scope.lookUp = (params) -> viewingContent.lookUp(params)
    
    $scope.goToPrevEntry = () -> viewingContent.goToPrevEntry()
    
    $scope.goToNextEntry = () -> viewingContent.goToNextEntry()
    
    $scope.goToStart = () -> viewingContent.goToStart()
  
  ]

)