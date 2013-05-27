define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Layout = ["$scope", "$location", ($scope, $location) ->
  
    # Used by the contentForTopic subview
    $scope.searchTopic = null
  
    $scope.panels = {
      left: false
      
      right: false
      
      top: null
      
      toggleLeft: () -> @left = not @left
      
      toggleRight: () -> @right = not @right

      toggleAddContent: () -> 
        @top = if (@top == "addContent") then null else "addContent"
        
      closeAddContent: () -> @top = null
     
      toggleTopMenu: () ->
        @top = if (@top == "menu") then null else "menu"
        
      toggleSlideSorter: () -> 
        @top = if (@top == "slideSorter") then null else "slideSorter"

      toggleEditDetails: () -> 
        @top = if (@top == "editDetails") then null else "editDetails"

      toggleEditContent: () -> 
        @top = if (@top == "editContent") then null else "editContent"

      toggleComments: () -> 
        @top = if (@top == "comments") then null else "comments"
        
      toggleContentForTopic: (topic) -> 
        @top = if (@top == "contentForTopic" and $scope.searchTopic == topic)
          null 
        else
          $scope.searchTopic = topic 
          "contentForTopic"
          
      closeTop: () -> @top = null 
    }
    
    $scope.$on('$routeUpdate', (path) ->
      $scope.panels.closeTop()
    )
  
    
  ]

)