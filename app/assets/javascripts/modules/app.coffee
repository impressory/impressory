
define(["./base", "services/ContentService", "services/CourseService", "services/UserService", "./QnAService", "./markdownService", "./viewingContent", "./viewingEvents" ], () ->
   
    Impressory.angularApp.config(['$locationProvider', ($locationProvider) ->
        $locationProvider.html5Mode(true)
    ])
    
    Impressory.angularApp.config(['$httpProvider', ($httpProvider) ->
        $httpProvider.defaults.headers.common['Accept']='application/json'
    ])
    
    # Handles route change errors so that the user doesn't just see a blank page
    Impressory.angularApp.controller('ErrorController', ['$scope', ($scope) ->
      $scope.$on("$routeChangeError", (event, current, previous, rejection) ->
        $scope.error = rejection
      )
      $scope.$on("$routeChangeSuccess", () ->
        $scope.error = null
      )
    ])
    
    
    Impressory.angularApp.config(['$routeProvider', ($routeProvider) ->  
        $routeProvider.
          when('/', { templateUrl: '/partials/main.html' }).
          when('/logIn', { templateUrl: '/partials/logIn.html' }).
          when('/signUp', { templateUrl: '/partials/signUp.html' }).
          when('/about', { templateUrl: '/partials/about.html' }).
          when('/createCourse', { templateUrl: '/partials/course/create.html' }).
          when('/self', { templateUrl: '/partials/user/self.html', controller: Impressory.Controllers.User.Self }).
          when('/course/:courseId', { templateUrl: '/partials/course/cover.html', resolve: Impressory.Controllers.Course.Cover.resolve, controller: Impressory.Controllers.Course.Cover }).
          when('/course/:courseId/activity', { templateUrl: '/partials/course/activityStream.html', resolve: Impressory.Controllers.Course.ActivityStream.resolve, controller: Impressory.Controllers.Course.ActivityStream }).
          when('/course/:courseId/editDetails', { templateUrl: '/partials/course/editDetails.html', resolve: Impressory.Controllers.Course.Edit.resolve, controller: Impressory.Controllers.Course.Edit }).
          when('/course/:courseId/invites', { templateUrl: '/partials/course/invites.html', resolve: Impressory.Controllers.Course.Invites.resolve, controller: Impressory.Controllers.Course.Invites  }).
          when('/course/:courseId/index', { templateUrl: '/partials/course/index.html', resolve: Impressory.Controllers.Course.Cover.resolve, controller: Impressory.Controllers.Course.Cover }).
          when('/course/:courseId/chatRoom', { templateUrl: '/partials/course/chatRoom.html', resolve: Impressory.Controllers.Course.Cover.resolve, controller: Impressory.Controllers.Course.Cover  }).
          when('/course/:courseId/view/lookup', { templateUrl: '/partials/course/viewContent.html', resolve: Impressory.Controllers.ViewContent.ViewContent.resolveLookup, controller: Impressory.Controllers.ViewContent.ViewContent  }).
          when('/course/:courseId/view/:entryId', { templateUrl: '/partials/course/viewContent.html', resolve: Impressory.Controllers.ViewContent.ViewContent.resolveView, controller: Impressory.Controllers.ViewContent.ViewContent  }).
          when('/course/:courseId/embedContent', { templateUrl: '/partials/course/embedContent.html', reloadOnSearch: false, resolve: Impressory.Controllers.ViewContent.ViewContent.resolve, controller: Impressory.Controllers.ViewContent.ViewContent  }).
          when('/course/:courseId/qna', { templateUrl: '/partials/qna/listQuestions.html', resolve: Impressory.Controllers.Course.Cover.resolve, controller: Impressory.Controllers.Course.Cover }).
          when('/course/:courseId/qna/new', { templateUrl: '/partials/qna/newQuestion.html', resolve: Impressory.Controllers.Course.Cover.resolve, controller: Impressory.Controllers.Course.Cover }).
          when('/course/:courseId/qna/:questionId', { templateUrl: '/partials/qna/viewQuestion.html', resolve: Impressory.Controllers.Course.Cover.resolve, controller: Impressory.Controllers.Course.Cover }).
          otherwise({ redirectTo: '/' })
      ])

    console.log("Angular app defined")

)