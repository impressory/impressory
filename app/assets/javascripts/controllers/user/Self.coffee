
define(["./base"], (l) ->

  # The part of the header that either shows login/signup or logout/user
  Impressory.Controllers.User.Self = ["$scope", "$http", ($scope, $http) ->
    $scope.login = Impressory.Model.Login
    
    $scope.logout = () -> 
      $http.post('/logOut')
        .success((data) -> 
          Impressory.Model.Login.logout()
        )
  ]
  
  Impressory.angularApp.directive("userTag", () -> 
    {
      restrict: 'E'
      scope: { user: '=user' }
      templateUrl: "directive_user_userTag.html"
    }
  )  
  
  
)