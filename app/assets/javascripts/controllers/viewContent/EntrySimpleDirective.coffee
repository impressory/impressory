define(["./base"], (l) ->


  Impressory.Controllers.ViewContent.EntrySimple = ["$scope", "ContentService", "$location", ($scope, ContentService, $location) ->



  ]


  Impressory.angularApp.directive("entrySimple", () ->
    {
      restrict: 'E'
      controller: Impressory.Controllers.AddContent.EntrySimple
      scope: { entry: '=' }
      template: """
        <div class="column-entry row" >

          <div class="header">
            <div class="who">
              <user-tag user="userCache.get(entry.addedBy)"></user-tag>
              <span class="pull-right created">{{ entry.created | date:'h:mma d MMM yy' }}</span>
            </div>
          </div>

          <div class="body">
            <h3 class="media-heading" ng-show="entry.title"><a ng-href="{{viewPath(entry)}}" ng-bind-html="entry.title"></a></h3>
            <p>{{ entry.note }}</p>


            <ce-render-entry-stream entry="entry"></ce-render-entry-stream>
            <div class="pull-right">
              <ce-show-tags entry="entry" on-adj-click="onTagClick(tag)" on-noun-click="onTagClick(tag)" on-topic-click="onTagClick(tag)" ></ce-show-tags>
            </div>

            <span style="border: 1px solid #eee; padding-bottom: 5px;">
              <entry-vote entry="entry"></entry-vote>
            </span>


              <button class="btn-link" ng-click="showComments=!showComments">{{ entry.comments.count }} <i class="fa fa-comments"></i></button>
          </div>

          <div class="footer" ng-show="showComments">
            <div class="comments">
              <entry-comments entry="entry"></entry-comments>
            </div>
          </div>
        </div>
      """
    }
  )

)