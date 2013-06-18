
define(["model/base"], () ->
 
  Impressory.angularApp = angular.module('impressory', ['ngSanitize'])
  
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
        when('/about', { templateUrl: 'partials/about.html' }).
        when('/createCourse', { templateUrl: 'partials/course/create.html' }).
        when('/course/:courseId', { templateUrl: '/partials/course/cover.html' }).
        when('/course/:courseId/invites', { templateUrl: '/partials/course/invites.html' }).
        when('/course/:courseId/listContent', { templateUrl: '/partials/course/listContent.html' }).
        when('/course/:courseId/index', { templateUrl: '/partials/course/index.html' }).
        when('/course/:courseId/chatRoom', { templateUrl: '/partials/course/chatRoom.html' }).
        when('/course/:courseId/viewContent', { templateUrl: '/partials/course/viewContent.html', reloadOnSearch: false }).
        when('/course/:courseId/qna', { templateUrl: '/partials/qna/listQuestions.html' }).
        when('/course/:courseId/qna/new', { templateUrl: '/partials/qna/newQuestion.html' }).
        when('/course/:courseId/qna/:questionId', { templateUrl: '/partials/qna/viewQuestion.html' }).
        otherwise({ redirectTo: '/' })
    ])
    
  
  # A directive that will put the content of the sandbox into an iframe
  Impressory.angularApp.directive('imSandbox', ['$compile', ($compile) -> 
    testTemplate = $compile("""
      <div>
        <html>
          <head>
          <link href='//cdnjs.cloudflare.com/ajax/libs/reveal.js/2.3/css/theme/sky.min.css' rel="stylesheet" ></link>          
          </head>
          <body>
          
          <div ng-bind-html="preview"></div>
          <script src='//cdnjs.cloudflare.com/ajax/libs/reveal.js/2.3/js/reveal.min.js'></script>
          <button onclick="Reveal.initialize()">start</button>
          </body>
        </html>
      </div>
    """)
    

    { 
      template: '<iframe style="border: none; height: 100%; width: 100%;"><base target="_parent"></iframe>'
      scope: true
      transclude: true
      controller: ['$scope', '$element', '$transclude', ($scope, $element, $transclude) -> 
        $scope.transclude = testTemplate($scope) ##
        null
      ]
      link: `function postLink(scope, ielement, iattrs, controller) { 
        var doc = ielement.find('iframe')[0].contentDocument
        doc.open(); 
        console.log(scope.transclude.html())
        doc.write(scope.transclude.html())
        doc.close()
        //$(doc).find('body').html(scope.transclude)
        
      }`     
    }])
  

  # A directive that will become a script tag
  # This solves the problem that if you just include a script tag in the partial HTML, it will be interpreted
  # by the browser as a script tag before the template is run.
  Impressory.angularApp.directive('imScript', () -> { 
    template: '<script>{}</script>'
    transclude: 'content'
    replace: true
    scope: true
    controller: ['$scope', '$element', '$transclude', ($scope, $element, $transclude) -> 
      $scope.transclude = $transclude
    ]
    link: `function postLink(scope, ielement, iattrs, controller) { 
      ielement.html(scope.transclude)
    }`     
    
  })

  # A directive that will become a script tag
  # This solves the problem that if you just include a script tag in the partial HTML, it will be interpreted
  # by the browser as a script tag before the template is run.
  Impressory.angularApp.directive('imLink', () -> { 
    template: '<link />'
    replace: true
  })

  
  Impressory.angularApp.directive('imViewingContent', () ->
  
    switch Impressory.Model.Viewing.Content.display?.kind?
      when 'sequence' then { templateUrl: 'foo' }
      else { templateUrl: 'nada' }
  
  )
  
  
    
  console.log("Angular app defined")

)