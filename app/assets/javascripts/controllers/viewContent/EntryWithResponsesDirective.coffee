define(["./base"], (l) ->


  Impressory.Controllers.ViewContent.EntryWithResponses = ["$scope", "ContentService", "$location", ($scope, ContentService, $location) ->

    $scope.viewEntryPath = () -> ContentService.viewEntryPath($scope.entry)

  ]


  Impressory.angularApp.directive("entryWithResponses", () ->
    {
      restrict: 'E'
      controller: Impressory.Controllers.AddContent.EntryWithResponses
      scope: { entry: '=' }
      template: """
        <div class="column-entry row" >

          <div class="header">
            <div class="who">
              <user-tag user-id="{{ entry.addedBy }}"></user-tag>
              <span class="pull-right created">{{ entry.created | date:'h:mma d MMM yy' }}</span>
            </div>
          </div>

          <div class="body">
            <h3 class="media-heading" ng-show="entry.message.title"><a ng-href="{{viewPath(entry)}}" ng-bind-html="entry.message.title"></a></h3>
            <p>{{ entry.message.note }}</p>


            <ce-render-entry-stream entry="entry"></ce-render-entry-stream>
            <div class="pull-right">
              <ce-show-tags entry="entry" on-adj-click="onTagClick(tag)" on-noun-click="onTagClick(tag)" on-topic-click="onTagClick(tag)" ></ce-show-tags>
            </div>

            <span style="border: 1px solid #eee; padding-bottom: 5px;">
              <entry-vote entry="entry"></entry-vote>
            </span>

            <button class="btn-link" ng-click="showResponses=!showResponses" ng-show="entry.settings.allowResponses">{{ entry.responses.count }} <i class="fa fa-mail-reply"></i></button>
            <button class="btn-link" ng-click="showComments=!showComments">{{ entry.comments.count }} <i class="fa fa-comments"></i></button>
          </div>

          <div class="footer" ng-show="showComments">
            <div class="comments">
              <entry-comments entry="entry"></entry-comments>
            </div>
          </div>
          <div class="footer" ng-show="showResponses">
            <div class="replies">
              <div >
                <entry-responses entry="entry" course="course"></entry-responses>
              </div>
            </div>
          </div>
        </div>
      """
    }
  )

)