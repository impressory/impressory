define(["./base"], (l) -> 

  Impressory.Controllers.AddContent.TopLevel = ["$scope", ($scope) ->
  
        
    $scope.categories = {
    
      "Special": [
        { 
          kind: "sequence", text: "Sequence", 
          help: """Sequences let you click through content in order. For example,
                  during a lecture. Content can be included in more than one sequence."""
        }
      ],
      
      "Text": [
        { 
          kind : "Markdown page", text: "Markdown",
          help: "A simple wiki page using Markdown format"
        }
      ],
      
      "Polls": [
        { 
          kind: "Multiple choice poll", text: "Multiple choice",
          help: "A live poll. Can be choose-one or choose-many"
        }
      ]
      
      
    
    }
     
    $scope.selection = {    
      category: "Web"    
      kind: "WebPage"    
    }
    
    $scope.setCategory = (cat) -> 
      $scope.selection.category = cat
      $scope.choices = $scope.categories[cat]
      $scope.choose(0)
      
    $scope.choose = (idx) -> 
      $scope.selection.kind = $scope.choices[idx].kind
      $scope.selection.help = $scope.choices[idx].help
      $scope.create.kind = $scope.selection.kind
      
    $scope.setCategory("Polls")
  ]

)