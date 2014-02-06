#
# Controller for log-in form.
#
define(["./base"], (l) ->

  Impressory.Controllers.LogIn.LogInForm = ["$scope", "UserService", "$http", "$location", ($scope, UserService, $http, $location) ->


    loginServices = []
    UserService.loginServicesList().then((list) -> loginServices = list)
    $scope.loginServiceEnabled = (name) ->
      loginServices.indexOf(name) >= 0

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