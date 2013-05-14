
define(["model/base"], () ->
  
  Impressory.angularApp = angular.module('impressory', ['ngResource'])
  
  Impressory.angularApp.config(['$locationProvider', ($locationProvider) ->
      $locationProvider.html5Mode(true)
  ])
  
  Impressory.angularApp.config(['$httpProvider', ($httpProvider) ->
      $httpProvider.defaults.headers.common['Accept']='application/json'
  ])
  
  Impressory.angularApp.config(['$routeProvider', ($routeProvider) ->  
      $routeProvider.
        when('/', { templateUrl: 'partials/main.html' }).
        when('/logIn', { templateUrl: 'partials/logIn.html' }).
        when('/signUp', { templateUrl: 'partials/signUp.html' }).
        when('/self', { templateUrl: 'partials/self.html' }).
        when('/help', { templateUrl: 'partials/help.html' }).
        when('/createCourse', { templateUrl: 'partials/course/create.html' }).
        when('/course/:courseId', { templateUrl: '/partials/course/cover.html' }).
        when('/course/:courseId/listContent', { templateUrl: '/partials/course/listContent.html' }).
        when('/course/:courseId/index', { templateUrl: '/partials/course/index.html' }).
        when('/course/:courseId/chatRoom', { templateUrl: '/partials/course/chatRoom.html' }).
        when('/course/:courseId/viewContent', { templateUrl: '/partials/course/viewContent.html', reloadOnSearch: false }).
        otherwise({ redirectTo: '/' })
    ])
    
  Impressory.angularApp.directive('imViewingContent', () ->
  
    switch Impressory.Model.Viewing.Content.display?.kind?
      when 'sequence' then { templateUrl: 'foo' }
      else { templateUrl: 'nada' }
  
  )
    
  console.log("Angular app defined")

)