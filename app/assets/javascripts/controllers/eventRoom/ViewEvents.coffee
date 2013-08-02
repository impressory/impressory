define(["./base"], (l) -> 

  Impressory.Controllers.EventRoom.ViewEvents = ['$scope', '$http', 'viewingEvents', ($scope, $http, viewingEvents) ->
  
    $scope.EventRoom = Impressory.Model.Viewing.EventRoom
    
    $scope.Users = Impressory.Model.Viewing.Users
    
    console.log("Course Id is " + $scope.courseId)
    
    # The parent scope will have set the value of courseId
    viewingEvents.showForCourse($scope.courseId)
  
    $scope.postChatMessage = (msg) -> 
      $http.post("/course/" + $scope.courseId + "/chat", msg)
      
    $scope.$on("push", () -> $scope.$apply())
  
  ]
  
  Impressory.angularApp.directive("chatViewEvents", () -> 
    {
      restrict: 'E'
      scope: { courseId: '=courseId', viewMode: '@' }
      templateUrl: "directive_chat_view_events.html"
    }
  )
  
  Impressory.angularApp.directive("chatForm", () -> 
    {
      restrict: 'E'
      scope: { courseId: '=courseId', viewMode: '@' }
      templateUrl: "directive_chat_form.html"
    }
  )
)