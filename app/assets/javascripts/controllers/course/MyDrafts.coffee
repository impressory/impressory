# Controller for the view that manages and creates invites

define(["./base"], (l) -> 

  Impressory.Controllers.Course.MyDrafts = ["$scope", "ContentService", "course", "entries", ($scope, ContentService, course, entries) ->
    $scope.course = course
    $scope.entries = entries
  ]

  Impressory.Controllers.Course.MyDrafts.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
    entries: ['$route', 'ContentService', ($route, ContentService) -> 
      ContentService.myDrafts($route.current.params.courseId)
    ] 
  }


)