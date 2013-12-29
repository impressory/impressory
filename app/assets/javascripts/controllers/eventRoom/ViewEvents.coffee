define(["./base"], (l) -> 

  Impressory.Controllers.EventRoom.ViewEvents = ['$scope', '$http', 'UserService', 'viewingEvents', ($scope, $http, UserService, viewingEvents) ->
  
    $scope.EventRoom = Impressory.Model.Viewing.EventRoom
    
    $scope.userCache = UserService.userCache()
    
    console.log("Course Id is " + $scope.courseId)
    
    # The parent scope will have set the value of courseId
    viewingEvents.showForCourse($scope.courseId)
  
    $scope.postChatMessage = () -> 
      $http.post("/course/" + $scope.courseId + "/chat", $scope.message)
      $scope.message = {}
      
    $scope.$on("push", () -> $scope.$apply())
    
    # Matches chat comments that are questions and should be highlighted
    questionRegex = /^Q:/    
    $scope.isQuestion = (text) -> questionRegex.test(text)
  
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