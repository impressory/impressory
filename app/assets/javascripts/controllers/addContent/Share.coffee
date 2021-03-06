define(["./base"], (l) ->


  Impressory.Controllers.AddContent.Share = ["$scope", "ContentService", "$location", ($scope, ContentService, $location) ->
    
    $scope.showPostMode = () ->
      $scope.errors = []
      $scope.postMode = "post"
      
    $scope.showShareMode = () ->
      $scope.errors = []
      $scope.postMode = "share"

    $scope.showCreateMode = () ->
      $scope.errors = []
      $scope.postMode = "create"

    $scope.whatIsIt = (code) ->    
      $scope.share = { }
      $scope.errors = [ ]
      $scope.inFlight = true

      $scope.whatIsItPromise = ContentService.whatIsIt(code).then(
        (data) -> $scope.share = data
      )
      
    $scope.remove = () ->
      $scope.share = { }
      $scope.errors = [ ]
      $scope.inFlight = false
      
    $scope.okToSubmit = () ->
      switch $scope.postMode
        when "post"
          $scope.write?.text 
        when "share"
          $scope.share.item
        when "create"
          true
          
    $scope.reset = () -> 
      $scope.postMode = "post"
      $scope.write = { }
      $scope.share = { }
      $scope.create = { } 
      $scope.errors = [ ]  
   
    $scope.submit = () ->
      if $scope.okToSubmit()
        switch $scope.postMode
          when "post"
            entry = {
              title: $scope.write.title
              note: $scope.write.note
              kind: "Markdown page"
              settings: {
                showFirst: false
                protect: false
                inTrash: false
                inNews: true
                inIndex: false
                allowResponses: true
                published: Date.now()
              }
              tags: {
                nouns: [ "News post" ]
                adjectives: []
                topics: []
              }
              item: {
                text: $scope.write.text
              }
            }
          
            ContentService.addContent($scope.course.id, entry)
            $scope.reset()
          when "share"
            entry = $scope.share
            entry.note = $scope.share?.text
            ContentService.addContent($scope.course.id, entry)
            $scope.reset()
          when "create"
            entry = {}
            entry.note = $scope.create?.text
            entry.kind = $scope.create.kind
            ContentService.addContent($scope.course.id, entry).then((created) ->
              path = ContentService.viewEntryPath(created)
              $location.path(path)
            )
            

    $scope.buttonText = () ->
      switch $scope.postMode
        when "post" then "Post"
        when "share" then "Share"
        when "create" then "Create"
            
    $scope.reset()
    
  ]


  Impressory.angularApp.directive("ceAddContent", () -> 
    {
      restrict: 'E'
      controller: Impressory.Controllers.AddContent.Share
      scope: { course: '=course', viewMode: '@', responseTo: '@', message: '@' }
      templateUrl: "directive_ce_add_content.html" 
    }
  )  

)