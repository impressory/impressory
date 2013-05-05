#
# Controller for sign-up form.
#
define(["./base"], (l) ->

  Impressory.Controllers.LogIn.SignUp = ["$scope", "$http", "$location", ($scope, $http, $location) ->    
    $scope.user = { }
    
    $scope.errors = []    
    
    $scope.submit = (user) -> 
      $scope.errors = [ ]
      $http.post('/signUp', user)
       .success((data) -> 
         if (data.user?)
           Impressory.Model.Login.login(data.user)
           $location.path("/")           
         if (data.error?)
           $scope.errors = [ data.error ]
         else
           console.log(data)
       )
       .error((data) ->
         $scope.errors = [ "Unexpected error" ]
         console.log(data)
       )
  ]

)