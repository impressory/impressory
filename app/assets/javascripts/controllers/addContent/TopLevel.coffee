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
      
      ],
      
      "Wiki": [
      
      ],
      
      "Poll": [
      
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