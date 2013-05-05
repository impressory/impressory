#
# Controller for log-in form.
#
define(["./base"], (l) ->

  Impressory.Controllers.LogIn.LogInForm = ["$scope", "$http", "$location", ($scope, $http, $location) ->    
    $scope.user = { }
    
    $scope.errors = []
    
    $scope.submit = (user) ->       
      $scope.errors = [ ]
      $http.post('/logInEP', user)
       .success((data) -> 
         console.log("success")
         console.log(data)        
         if (data.user?)
           Impressory.Model.Login.login(data.user)
           $location.path("/")
         if (data.error?)
           $scope.errors = [ data.error ]
         else
           console.log(data.error)
       )
       .error((data) ->
         $scope.errors = [ "Unexpected error" ]
         console.log(data)
       )
  ]
  
)