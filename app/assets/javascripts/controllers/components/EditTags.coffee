#
# Reusable component that handles adding and removing tags from a model.
# This is a controller that resides as a sub-scope in other forms.
# The parent must define $scope.entry
#


define(["./base"], (l) -> 

  Impressory.Controllers.Components.EditTags = ["$scope", "$http", ($scope) ->
    
    console.log($scope.entry)
    
    $scope.toAdd = ""    
    
    $scope.addAdj = (adj) -> 
      if (adj and not (adj in $scope.entry.adjectives)) then $scope.entry.adjectives.push(adj)
      $scope.toAdd = ""

    $scope.addNoun = (noun) -> 
      if (noun and not (noun in $scope.entry.nouns)) then $scope.entry.nouns.push(noun)
      $scope.toAdd = ""

    $scope.addTopic = (topic) -> 
      if (topic and not (topic in $scope.entry.topics)) then $scope.entry.topics.push(topic)
      $scope.toAdd = ""

    $scope.deleteAdj = (a) -> $scope.entry.adjectives = $scope.entry.adjectives.filter((adj) -> adj != a)

    $scope.deleteNoun = (n) ->  $scope.entry.nouns = $scope.entry.nouns.filter((noun) -> noun != n)

    $scope.deleteTopic = (t) ->  $scope.entry.topics = $scope.entry.topics.filter((top) -> top != t)
    
  ]

)