define(["./base"], (l) -> 

  Impressory.Controllers.AddContent.TopLevel = ["$scope", ($scope) ->
  
        
    $scope.categories = {
    
      "Web": [
        { kind: "web page", text: "Web page" }
      ],
      
      "Presentation": [
        { kind: "Google Slides", text: "Google Slides" }
      ],

      "Video": [
        { kind: "YouTube video", text: "YouTube" }
      ],
      
      "Sequence": [
        { kind: "sequence", text: "Content Sequence" }
      ],
      
      "Broadcast": [
        { kind : "YouTube video", text: "YouTube" }
      ],
      
      "Wiki": [
        { kind : "Markdown page", text: "Markdown" },
        { kind : "Structured wiki", text: "Page builder" }
      ],
      
      "Poll": [
        { kind : "Pick poll", text: "Tick the box" },
        { kind : "Free text poll", text: "Free text poll" }
      
      ]
      
      
    
    }
     
    $scope.selection = {    
      category: "Web"    
      kind: "WebPage"    
    }
    
    $scope.setCategory = (cat) -> 
      $scope.selection.category = cat
      $scope.choices = $scope.categories[cat]
      $scope.setKind($scope.choices[0].kind)
      
    $scope.setKind = (kind) -> $scope.selection.kind = kind
      
    $scope.setCategory("Web")
  ]

)