define(["./base"], (l) ->


  # The part of the header that either shows login/signup or logout/user
  Impressory.Controllers.User.UserTag = ["$scope", "UserService", ($scope, UserService) ->

    UserService.get($scope.userId).then((user) -> $scope.user = user)
  ]

  Impressory.angularApp.directive("userTag", () ->
    {
      restrict: 'E'
      scope: { userId: '@' }
      controller: Impressory.Controllers.User.UserTag
      template: """
        <span class="user-tag">
          <span class="avatar"><img ng-src="{{ user.avatar || 'http://placehold.it/64x64' }}" alt="User's avatar"/></span>
          <span class="nickname">{{ user.nickname || 'Anonymous' }}</span>
        </span>
      """
    }
  )


)