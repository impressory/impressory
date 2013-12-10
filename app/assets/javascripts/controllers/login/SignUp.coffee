#
# Controller for sign-up form.
#
define(["./base"], (l) ->

  Impressory.Controllers.LogIn.SignUp = ["$scope", "$http", "$location", ($scope, $http, $location) ->    
    $scope.user = { }
    
    $scope.errors = []    
    
    $scope.submit = (user) -> 
      $scope.errors = [ ]
      $http.post('/signUp', user).success((data) -> 
        Impressory.Model.Login.login(data)
        $location.path("/")           
      ).error((data) ->
        $scope.errors = [ data ]
        console.log(data)
      )
  ]

)