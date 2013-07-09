define(["./base"], (l) -> 

  Impressory.Controllers.Components.ListEntries = ["$scope", "ContentService", "viewingUsers", ($scope, ContentService, viewingUsers) ->
  
    $scope.viewMode = $scope.viewMode || 'rows'
    $scope.viewAsRows = () -> $scope.viewMode = 'rows'
    $scope.viewAsColumns = () -> $scope.viewMode = 'columns'
    
    $scope.viewPath = (entry) -> ContentService.viewPath(entry)
    
    $scope.users = Impressory.Model.Viewing.Users
    
    updateUsers = () -> 
      # Look up any users we haven't cached 
      users = (entry.addedBy for entry in ($scope.entries || []))
      viewingUsers.request(users)
      
    $scope.$watch('entries', (nv, ov) -> updateUsers())
      
    updateUsers()
  ]
  
  Impressory.angularApp.directive("listEntries", () -> 
    {
      restrict: 'E'
      scope: { entries: '=entries', viewMode: '@' }
      templateUrl: "directive_listEntries.html"
    }
  )

)