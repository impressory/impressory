define(["./base"], (l) ->

  Impressory.Controllers.ViewContent.EntrySimple = ["$scope", "ContentService", "$location", ($scope, ContentService, $location) ->
    $scope.viewPath = (e) ->
      ContentService.viewEntryPath(e)
  ]


  Impressory.angularApp.directive("entryRow", () ->
    {
      restrict: 'E'
      scope: { entry: '=' }
      template: """
        <div class="row-entry">
          <div class="row">
            <div class="col-sm-1">
              <span class="badge badge-warning score">{{ entry.voting.score || 0 }} pts</span>
              <span class="badge badge-info score">{{ entry.comments.count || 0 }} <i class="icon-comments"></i></span>
              <span class="badge badge-error" ng-show="entry.protect"><i class="icon-key"></i></span>
            </div>
            <div class="col-sm-2">
              <ce-show-tags entry="entry" on-adj-click="onTagClick(tag)" on-noun-click="onTagClick(tag)" on-topic-click="onTagClick(tag)"></ce-show-tags>
            </div>
            <div class="col-sm-5">
              <div class="title"><a ng-href="{{viewPath(entry)}}">{{ entry.message.title || 'Untitled' }}</a></div>
              <div class="note">{{ entry.message.note }}</div>
              <div class="info">
                <div class="entry-id">{{ entry.id }}</div>
                <div>
                  <span class="kind">{{ entry.kind }}</span>
                  <span class="site">at {{ entry.tags.site }}</span>
                </div>
                <span class="updated">updated {{ entry.updated | date:'h:mma d MMM yy' }}</span>
              </div>
            </div>
            <div class="col-sm-4">
              <div class="ab-label">added by</div>
              <div class="who">
                <user-tag user-id="{{ entry.addedBy }}"></user-tag>
              </div>
              <div class="created">{{ entry.created | date:'h:mma d MMM yy' }}</div>
            </div>
          </div>
        </div>
      """
    }
  )

)