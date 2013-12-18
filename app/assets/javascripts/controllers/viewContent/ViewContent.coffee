define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.ViewContent = ["$scope", "$routeParams", "course", "entry", ($scope, $routeParams, course, entry) ->
    $scope.course = course
    $scope.courseId = course.id
    $scope.entry = Impressory.Model.Viewing.Content.entry
    
    # The content currently showing, taking sequences into account
    $scope.display = () -> 
      Impressory.Model.Viewing.Content.seqEntry ? $scope.entry 
        
  ]
  
  Impressory.Controllers.ViewContent.ViewContent.resolveLookup = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
    entry: ['$route', 'ContentService', ($route, ContentService) ->    
      courseId = $route.current.params.courseId
      entryQuery = {
        entryId: $route.current.params.entryId
        adj: $route.current.params.adj
        noun: $route.current.params.noun
        topic: $route.current.params.topic
        site: $route.current.params.site
      }
      ContentService.lookUp(courseId, entryQuery).then((eis) ->
        Impressory.Model.Viewing.Content.entry = eis.entry
        Impressory.Model.Viewing.Content.goToSeqIndex = eis.seqIndex
        Impressory.Model.Viewing.Content.seqEntry = null
      )
    ]
  }
  
  Impressory.Controllers.ViewContent.ViewContent.resolveView = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
    entry: ['$route', 'ContentService', ($route, ContentService) ->
      entryId = $route.current.params.entryId
      
      # Check to see if we're already looking at it
      if (Impressory.Model.Viewing.Content.entry?.id == entryId) 
        Impressory.Model.Viewing.Content.goToSeqIndex = -1
        Impressory.Model.Viewing.Content.entry
      else
        idx = Impressory.Model.Viewing.Content.entry?.item?.entries?.indexOf(entryId)
        if (idx >= 0) 
          Impressory.Model.Viewing.Content.goToSeqIndex = idx
          Impressory.Model.Viewing.Content.entry
        else
          ContentService.get($route.current.params.courseId, ).then((entry) ->
            Impressory.Model.Viewing.Content.entry = entry
            Impressory.Model.Viewing.Content.goToSeqIndex = -1
            Impressory.Model.Viewing.Content.seqEntry = null
          )
    ]
  }

)