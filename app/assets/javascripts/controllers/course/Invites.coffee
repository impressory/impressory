# Controller for the view that manages and creates invites

define(["./base"], (l) -> 

  Impressory.Controllers.Course.Invites = ["$scope", "CourseService", "course", "invites", ($scope, CourseService, course, invites) ->
  
    $scope.course = course
    
    $scope.invites = invites
  
    $scope.create = (newInvite) -> CourseService.createInvite(course.id, newInvite).then((invite) -> 
      $scope.invites = CourseService.fetchInvites(course.id)
    )
     
  ]

  Impressory.Controllers.Course.Invites.resolve = {
    course: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.get($route.current.params.courseId)
    ]
    invites: ['$route', 'CourseService', ($route, CourseService) -> 
      CourseService.fetchInvites($route.current.params.courseId)
    ] 
  }


)