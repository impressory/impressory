define ['../app'], () ->

  angular.module("impressory")
    .controller('editMarkdown', ['$scope', '$sce', 'MarkdownService', ($scope, $sce, MarkdownService) ->
      $scope.previewing = false
    ])
    .directive('editMarkdown', () -> {
      restrict: 'E'
      controller: 'editMarkdown'
      scope: { 'text': '=', 'prompt': '@' }
      template: """
        <div class="">
            <div class="clearfix">
              <span class="pull-right">
                  <a target="_blank" href="//daringfireball.net/projects/markdown/syntax#header"><small>Markdown</small></a>
                  <button class="btn btn-default btn-xs" ng-click="previewing = !previewing">
                      <span ng-hide="previewing" class="glyphicon glyphicon-eye-open"></span>
                      <span ng-show="previewing" class="glyphicon glyphicon-eye-close"></span>
                  </button>
              </span>
            </div>
            <div >
              <textarea class="form-control" ng-hide="previewing" ng-model="text" placeholder="{{ prompt }}"></textarea>
              <div class="panel panel-default" ng-if="previewing">
                <div class="panel-body">
                  <render-markdown class="form-control-static" text="text" ></render-markdown>
                </div>
              </div>
            </div>
        </div>
      """
    })