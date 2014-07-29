define(["./base"], (l) ->

  Impressory.Controllers.ViewContent.EntryComments = ["$scope", "ContentService", "UserService", ($scope, ContentService, UserService) ->



  ]

  Impressory.angularApp.directive("entryComments", () ->
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.EntryComments
      scope: { entry: '=entry', course: '=' }
      template: """
        <div class="view-entry-comments sidemargin" ng-controller="Impressory.Controllers.ViewContent.Comments">
          <div class="comments">
            <div ng-repeat="comment in entry.comments.embedded" class="embedded-comment">
              <entry-comment comment="comment"></entry-comment>
            </div>
          </div>

          <form class="form">
            <textarea name="comment" ng-disabled="!entry.permissions.comment" class="form-control" placeholder="Leave a comment" ng-model="newComment.text"></textarea><br />
            <button class="btn btn-primary" type="button" ng-show="newComment.text" ng-disabled="!entry.permissions.comment" ng-click="addComment(newComment)">Comment</button>
          </form>
        </div>
      """
    }
  )
)