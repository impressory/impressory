define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.TopNav = ["$scope", "viewingContent", ($scope, viewingContent) ->



  ]


  Impressory.angularApp.directive("ceTopNav", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.TopNav
      scope: { entry: '=entry', panels: "=panels", course: "=course", viewMode: '@' }
      templateUrl: "directive_ce_top_nav.html" 
    }
  )  

)
