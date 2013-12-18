#
# Form for editing the details of a single ContentEntry.
# Components.EditTags is a subcontroller handling the adjective, noun, and topic tags.
#

define(["./base"], (l) -> 

  Impressory.Controllers.ViewContent.EditDetails = ["$scope", "ContentService", ($scope, ContentService) ->
    
    unedited = $scope.entry
    
    $scope.entry = angular.copy($scope.entry)
        
    $scope.errors = []
    
    embedUrl = ContentService.embedUrl($scope.entry.course, $scope.entry.id)
    
    $scope.embedCode = "<iframe width='800' height=600' style='border:none; scrolling: no;' src='#{embedUrl}'></iframe>"
    
    $scope.submit = () ->
    
      $scope.errors = []
       
      ContentService.editTags($scope.entry).then(
        (entry) -> 
          # Update the parent's scope entry that we were asked to edit 
          angular.copy(entry, unedited)
        
          $scope.panels.toggleEditDetails()
        (data) ->
          console.log(data)
          $scope.errors = [ data.error || "Unexpected error" ]
      )
  ]

)