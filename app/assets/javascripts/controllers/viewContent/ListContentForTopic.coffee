define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.ListContentForTopic = ["$scope", "$http", "viewingUsers", ($scope, $http, viewingUsers) ->

    $scope.entries = []
    
    $scope.users = Impressory.Model.Viewing.Users
    
    runSearch = (topic) -> 
      $scope.entries = $http.get("entriesForTopic", { params: { topic : topic } }).then((res) ->
        console.log(res)
        entries = res.data.entries 
        
        # Look up any users we haven't cached 
        users = (entry.addedBy for entry in entries)
        viewingUsers.request(users)
        
        entries
      )

    $scope.$watch('searchTopic', (newVal, oldVal) -> runSearch(newVal))
    
    
    
  ]

)