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
      
      viewingContent.lookUp($scope.entryQuery).then((data) -> $scope.entry = data)
      
      
      
    updateView()
      
    $scope.$on('$routeUpdate', updateView)
    
    $scope.viewing = Impressory.Model.Viewing
    
    
    $scope.watch('viewing.Content.diplay', (nv, ov) -> $scope.entry = nv)
    
    # Used by ng-click in slide-sorter, amongst others
    $scope.lookUp = (params) -> viewingContent.lookUp(params)
    
    $scope.goToPrevEntry = () -> viewingContent.goToPrevEntry()
    
    $scope.goToNextEntry = () -> viewingContent.goToNextEntry()
    
    $scope.goToStart = () -> viewingContent.goToStart()
  
  ]
  
  Impressory.Controllers.ViewContent.ViewContent.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
    entry: ['$route', 'viewingContent', ($route, viewingContent) ->
    
      $scope.entryQuery = {
        courseId: $route.current.params.courseId
        entryId: $route.current.params.entryId
        adj: $route.current.params.adj
        noun: $route.current.params.noun
        topic: $route.current.params.topic
        site: $route.current.params.site
      }
      
      viewingContent.lookUp($scope.entryQuery)
    ]
  }
  

)