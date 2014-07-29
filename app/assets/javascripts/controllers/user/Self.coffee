
define(["./base"], (l) ->

  # The part of the header that either shows login/signup or logout/user
  Impressory.Controllers.User.Self = ["$scope", "$http", ($scope, $http) ->

    # Allows template to set flags for which panels should be visible
    # These flags are set within a referenced object, so that subscopes (prototypical inheritance)
    # will update the value rather than hide it
    $scope.show = {}

    $scope.login = Impressory.Model.Login

    $scope.logout = () ->
      $http.post('/logOut')
        .success((data) ->
          Impressory.Model.Login.logout()
        )
  ]
  
)