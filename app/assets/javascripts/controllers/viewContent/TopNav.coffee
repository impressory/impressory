define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.TopNav = ["$scope", ($scope) ->

    # Used by the contentForTopic subview
    $scope.searchTopic = null
  
    $scope.panels = {
    
      minimisedHeader: false
      
      minimiseHeader: () -> 
        @minimisedHeader = true
    
      top: null
      
      toggleLeft: () -> @left = not @left
      
      toggleRight: () -> @right = not @right

      toggleAddContent: () -> 
        @top = if (@top == "addContent") then null else "addContent"
        
      closeAddContent: () -> @top = null
     
      toggleTopMenu: () ->
        @top = if (@top == "menu") then null else "menu"
        
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


  Impressory.angularApp.directive("ceTopNav", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.ViewContent.TopNav
      scope: { entry: '=entry', course: "=course", viewMode: '@' }
      templateUrl: "directive_ce_top_nav.html" 
    }
  )  

)
