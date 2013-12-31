define(["./base"], (l) -> 

  Impressory.Controllers.Course.WrapWithChat = ["$scope", ($scope) ->
  
    $scope.chatOpen = () ->
      Impressory.Model.Viewing.Display.chatOpen
      
    $scope.toggleChat = () -> 
      Impressory.Model.Viewing.Display.chatOpen = !Impressory.Model.Viewing.Display.chatOpen
  ]
  
  Impressory.angularApp.directive("courseWrapWithChat", () -> 
    {
      restrict: 'E'
      transclude: true
      controller: Impressory.Controllers.Course.WrapWithChat
      scope: { course: '=course' }
      templateUrl: "directive_course_wrap_with_chat.html"
    }
  )
  
  Impressory.angularApp.directive("courseChatToggle", () -> 
    {
      restrict: 'A'
      controller: Impressory.Controllers.Course.WrapWithChat
    }
  )

)