
define(["model/base"], () ->
 
  Impressory.angularApp = angular.module('impressory', ['ngRoute', 'ngSanitize'])
  
  
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
  

)