define(["./base"], (l) ->

  Impressory.Controllers.ViewContent.EntryResponses = ["$scope", "ContentService", "UserService", ($scope, ContentService, UserService) ->

    $scope.responses = [];

    $scope.updateResponses = () ->
      ContentService.request(
        $scope.entry.course,
        $scope.entry.responses.entries
      ).then((entries) ->
        $scope.responses = entries
      )

    $scope.$watch(
      () -> $scope.entry.responses.count,
      (nv, ov) ->
        $scope.updateResponses()
    )

  ]

  Impressory.angularApp.directive("entryResponses", () ->
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.EntryResponses
      scope: { entry: '=entry', course: '=' }
      template: """
        <h3>Responses</h3>

        <div ng-repeat="response in responses">
          <entry-simple entry="response"></entry>
        </div>

        <entry-add-response response-to="entry"></entry-add-response>
      """
    }
  )
)