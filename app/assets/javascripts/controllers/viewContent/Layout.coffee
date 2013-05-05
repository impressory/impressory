define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.Layout = ["$scope", ($scope) ->
  
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

    }
  ]

)