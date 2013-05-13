define(["./base"], (l) -> 

  Impressory.Controllers.EventRoom.ViewEvents = ['$scope', '$http', 'viewingEvents', ($scope, $http, viewingEvents) ->
  
    $scope.EventRoom = Impressory.Model.Viewing.EventRoom
    
    $scope.Users = Impressory.Model.Viewing.Users
    
    console.log("Course Id is " + $scope.courseId)
    
    # The parent scope will have set the value of courseId
    viewingEvents.showForCourse($scope.courseId)
  
    $scope.postChatMessage = (msg) -> 
      $http.post("/course/" + $scope.courseId + "/chat", msg)
      
    $scope.$on("chat", () -> $scope.$apply())
  
  ]
)