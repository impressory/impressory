#
# Form for editing the details of a single ContentEntry.
# Components.EditTags is a subcontroller handling the adjective, noun, and topic tags.
#

define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.EditDetails = ["$scope", "$http", "ContentService", ($scope, $http, ContentService) ->
    
    $scope.entry = angular.copy(Impressory.Model.Viewing.Content.display)
        
    $scope.errors = []
    
    embedUrl = ContentService.embedUrl($scope.entry.course, $scope.entry.id)
    
    $scope.embedCode = "<iframe width='800' height=600' style='border:none; scrolling: no;' src='#{embedUrl}'></iframe>"
    
    $scope.submit = () ->
    
      $scope.errors = []
       
      $http.post("/course/" + $scope.entry.course + "/entry/" + $scope.entry.id + "/editTags", $scope.entry).success((data) ->
         if (data.error?)
           $scope.errors = [ data.error ]
         else if (data.course?)
           # Update the entry in memory to match the returned data
           angular.copy(data.course, Impressory.Model.Viewing.Content.display)
           $scope.panels.toggleEditDetails()
      ).error((data) -> 
         console.log(data)
         $scope.errors = [ data.error || "Unexpected error " ]
      )

  ]

)