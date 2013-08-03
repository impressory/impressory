define(["./base"], (l) ->

  # If this controller is present on the page, it will send route information to the server whenever it changes.
  # This allows collecting basic learning analytics such as which pieces of content are being viewed and when.
  Impressory.Controllers.Analytics.ViewAnalytics = ['$scope', '$location', '$route', '$http', ($scope, $location, $route, $http) ->
    
    postPageView = () -> 
      $http.post("/pageView", {
        template: $route.current.templateUrl,
        params: $route.current.params
      })
    
    $scope.$on("$routeChangeSuccess", () -> postPageView())
    $scope.$on("$routeUpdate", () -> postPageView())
  ]


)