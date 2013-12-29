
define(["./base"], (l) ->

  # The part of the header that either shows login/signup or logout/user
  Impressory.Controllers.User.ChangePassword = ["$scope", "UserService", ($scope, UserService) ->

    $scope.submit = () -> 
      UserService.changePassword($scope.oldPassword, $scope.newPassword).then(
        (user) ->
          console.log("us it was success")
          console.log(user) 
          $scope.close()
        ,
        (error) -> 
          console.log("us it was error")
          $scope.errors = [ error ] 
      )

  ]

  Impressory.angularApp.directive("userChangePasswordForm", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.User.ChangePassword
      scope: { 'close': '&onClose' }
      templateUrl: "directive_user_changePassword.html" 
    }
  )  
)