define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.ViewContent = ["$scope", "$routeParams", "viewingContent", "course", "entry", ($scope, $routeParams, viewingContent, course, entry) ->

    $scope.course = course
    $scope.courseId = course.id

    $scope.entry = entry
    
    $scope.viewing = Impressory.Model.Viewing
    $scope.$watch('viewing.Content.display', (newVal, oldVal) -> $scope.entry = newVal)
    
    # ViewContent does not completely refresh when the location's search (parameters) change.
    # Instead we handle changes to the search (parameters) from within the controller by listening to the route.
    # The reason for this is to avoid reloading every iframe in a content sequence as you
    # advance through it
    #
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
    $scope.$on('$routeUpdate', updateView)
    
    # Used by ng-click in slide-sorter, amongst others
    $scope.lookUp = (params) -> viewingContent.lookUp(params)
    
  ]
  
  Impressory.Controllers.ViewContent.ViewContent.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
    entry: ['$route', 'viewingContent', ($route, viewingContent) ->
    
      entryQuery = {
        courseId: $route.current.params.courseId
        entryId: $route.current.params.entryId
        adj: $route.current.params.adj
        noun: $route.current.params.noun
        topic: $route.current.params.topic
        site: $route.current.params.site
      }
      
      viewingContent.lookUp(entryQuery)
    ]
  }
  

)