define(["./base"], (l) -> 

  Impressory.Controllers.Components.ListEntries = ["$scope", "ContentService", "UserService", ($scope, ContentService, UserService) ->
  
    $scope.viewMode = $scope.viewMode || 'rows'
    $scope.viewAsRows = () -> $scope.viewMode = 'rows'
    $scope.viewAsColumns = () -> $scope.viewMode = 'columns'
    
    $scope.viewPath = (entry) -> ContentService.viewEntryPath(entry)
    
    $scope.userCache = UserService.userCache()
    
    updateUsers = () -> 
      # Look up any users we haven't cached 
      users = (entry.addedBy for entry in ($scope.entries || []))
      UserService.request(users)
      
    $scope.$watch('entries', (nv, ov) -> updateUsers())
    
    # String to look for in tags
    $scope.tagFilter = null
    
	# Returns true for only those entries whose tags contain the tagFilter string
    applyTagFilter = (entry) ->
      !($scope.tagFilter?) || ( 
        tags = entry.tags.topics.concat(entry.tags.nouns).concat(entry.tags.adjs)
        tags.filter((tag) -> tag?.indexOf($scope.tagFilter) >= 0).length > 0
      )      
      
    $scope.onTagClick = (tag) ->
      console.log(tag)
      $scope.tagFilter = tag

    # Filters entries to show	
    $scope.applyFilter = (entry) ->
      applyTagFilter(entry)        
      
    updateUsers()
  ]
  
  Impressory.angularApp.directive("listEntries", () -> 
    {
      restrict: 'E'
      scope: { entries: '=entries', viewMode: '@' }
      templateUrl: "directive_listEntries.html"
    }
  )
  
  Impressory.Controllers.Components.BtnOpenInViewer = ["$scope", "ContentService", ($scope, ContentService) ->
    $scope.viewPath = (entry) -> ContentService.viewEntryPath(entry)
  ]  
  
  Impressory.angularApp.directive("btnOpenInViewer", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.Components.BtnOpenInViewer
      scope: { entry: '=entry' }
      template: "<a class='btn btn-default btn-small' ng-href='{{viewPath(entry)}}'>Open in viewer</a>"
    }
  )  

)