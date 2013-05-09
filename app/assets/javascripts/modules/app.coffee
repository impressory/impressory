
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
        when('/createCourse', { templateUrl: 'partials/course_create.html' }).
        when('/course/:courseId', { templateUrl: '/partials/course_cover.html' }).
        when('/course/:courseId/listContent', { templateUrl: '/partials/course_listContent.html' }).
        when('/course/:courseId/viewContent', { templateUrl: '/partials/course_viewContent.html' }).
        otherwise({ redirectTo: '/' })
    ])
    
  Impressory.angularApp.directive('imViewingContent', () ->
  
    switch Impressory.Model.Viewing.Content.display?.kind?
      when 'sequence' then { templateUrl: 'foo' }
      else { templateUrl: 'nada' }
  
  )
    
  console.log("Angular app defined")

)