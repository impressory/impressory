define(["./base"], (l) ->

  Impressory.Controllers.Components.RequestSpinner = ["$scope", "ContentService", "UserService", ($scope, ContentService, UserService) ->

    # The progression to go through for a new promise.
    # First, we show a loading spinner (while the request completes)
    # Then we wither show the transcluded content on success,
    # or the error message on failure
    start = () ->
      console.log("starting spinner")
      $scope.loading = $scope.promise?
      $scope.error = null
      $scope.success = true
      $scope.promise?.then(
        (res) ->
          $scope.loading = false
          $scope.success = true
        ,
        (errRes) ->
          $scope.loading = false
          $scope.error = errRes
      )

    # Upon being given a new $promise, start the animation
    $scope.$watch("promise", (nv) -> if (nv) then start())

  ]

  Impressory.angularApp.directive("imRequestSpinner", () ->
    {
      restrict: 'E'
      transclude: true
      scope: { promise: '=promise' }
      controller: Impressory.Controllers.Components.RequestSpinner
      templateUrl: "directive_requestSpinner.html"
    }
  )

)