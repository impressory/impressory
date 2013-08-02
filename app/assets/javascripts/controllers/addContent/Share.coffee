define(["./base"], (l) ->

  Impressory.Controllers.AddContent.Share = ["$scope", "ContentService", ($scope, ContentService) ->
  
    
    
    $scope.showPostMode = () ->
      $scope.errors = []
      $scope.postMode = "post"
      
    $scope.showShareMode = () ->
      $scope.errors = []
      $scope.postMode = "share"

    $scope.whatIsIt = (code) ->    
      $scope.toAdd = { }
      $scope.errors = [ ]
      $scope.inFlight = true
      ContentService.whatIsIt(code).then(
        (res) -> $scope.toAdd = res.data,
        (res) -> $scope.errors = [ res.data.error ]
      )
      
    $scope.remove = () ->
      $scope.toAdd = { }
      $scope.errors = [ ]
      $scope.inFlight = false
      
    $scope.okToSubmit = () ->
      switch $scope.postMode
        when "post"
          $scope.share?.text 
        when "share"
          $scope.toAdd.item
          
    $scope.reset = () -> 
      $scope.postMode = "post"  
      $scope.toAdd = { }
      $scope.errors = [ ]  
   
    $scope.submit = () ->
      if $scope.okToSubmit()
        switch $scope.postMode
          when "post"
            $scope.errors = [ "Whoops, we haven't done post yet!" ]
          when "share"
            entry = $scope.toAdd
            entry.note = $scope.share?.text
            ContentService.addContent($scope.course.id, entry)
            $scope.reset()
   
   
    $scope.reset()
    
    
  ]

)