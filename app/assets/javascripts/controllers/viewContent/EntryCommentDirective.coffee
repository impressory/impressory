define(["./base"], (l) ->

  Impressory.Controllers.ViewContent.EntryComment = ["$scope", "ContentService", "UserService", ($scope, ContentService, UserService) ->

  ]

  Impressory.angularApp.directive("entryComment", () ->
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.EntryComment
      scope: { comment: '=comment', course: '=' }
      template: """
        <div class="text">{{ comment.text }}</div>
        <div class="who">
          <user-tag user="userCache.get(comment.addedBy)"></user-tag>
          at {{ comment.created | date:'h:mma d MMM yy' }}
        </div>
      """
    }
  )
)