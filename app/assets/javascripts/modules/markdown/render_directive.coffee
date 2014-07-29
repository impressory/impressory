define ['../app'], () ->

  angular.module("impressory")
    .controller('renderMarkdown', ['$scope', '$sce', 'MarkdownService', ($scope, $sce, MarkdownService) ->
      $scope.$watch("text", (nv) ->
        $scope.output = $sce.trustAsHtml(MarkdownService.render(nv))
      )
    ])
    .directive('renderMarkdown', () -> {
      restrict: 'E'
      controller: 'renderMarkdown'
      scope: { 'text': '=' }
      template: "<div ng-bind-html='output'></div>"
    })