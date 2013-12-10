#
# Controller for log-in form.
#
define(["./base"], (l) ->

  Impressory.Controllers.LogIn.LogInForm = ["$scope", "$http", "$location", ($scope, $http, $location) ->    
    $scope.user = { }
    
    $scope.errors = []
    
    $scope.submit = (user) ->       
      $scope.errors = [ ]
      $http.post('/logInEP', user).success((data) -> 
        Impressory.Model.Login.login(data)
        $location.path("/")
      ).error((data) ->
        $scope.errors = [ data ]
        console.log(data)
      )
  ]
  
)