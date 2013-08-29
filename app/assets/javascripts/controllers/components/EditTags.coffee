#
# Reusable component that handles adding and removing tags from a model.
# This is a controller that resides as a sub-scope in other forms.
# The parent must define $scope.entry
#


define(["./base"], (l) -> 

  Impressory.Controllers.Components.EditTags = ["$scope", "$http", ($scope) ->
    
    $scope.toAdd = ""    
    
    $scope.addAdj = (adj) -> 
      if (adj and not (adj in $scope.entry.tags.adjectives)) then $scope.entry.tags.adjectives.push(adj)
      $scope.toAdd = ""

    $scope.addNoun = (noun) -> 
      if (noun and not (noun in $scope.entry.tags.nouns)) then $scope.entry.tags.nouns.push(noun)
      $scope.toAdd = ""

    $scope.addTopic = (topic) -> 
      if (topic and not (topic in $scope.entry.tags.topics)) then $scope.entry.tags.topics.push(topic)
      $scope.toAdd = ""

    $scope.deleteAdj = (a) -> $scope.entry.tags.adjectives = $scope.entry.tags.adjectives.filter((adj) -> adj != a)

    $scope.deleteNoun = (n) ->  $scope.entry.tags.nouns = $scope.entry.tags.nouns.filter((noun) -> noun != n)

    $scope.deleteTopic = (t) ->  $scope.entry.tags.topics = $scope.entry.tags.topics.filter((top) -> top != t)
    
  ]
  
  
  Impressory.angularApp.directive("ceShowTags", () -> 
    {
      restrict: 'E'
      scope: { entry: '=entry', viewMode: '@' }
      templateUrl: "directive_ce_show_tags.html"
    }
  )  

  Impressory.angularApp.directive("ceEditTags", () -> 
    {
      restrict: 'E'
      scope: { entry: '=entry', viewMode: '@' }
      templateUrl: "directive_ce_edit_tags.html"
    }
  )  

  Impressory.angularApp.directive("ceEditSettings", () -> 
    {
      restrict: 'E'
      scope: { entry: '=entry', viewMode: '@' }
      templateUrl: "directive_ce_edit_settings.html"
    }
  )  
)