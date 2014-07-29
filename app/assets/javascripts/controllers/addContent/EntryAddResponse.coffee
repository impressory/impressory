define(["./base"], (l) ->


  Impressory.Controllers.AddContent.EntryAddResponse = ["$scope", "ContentService", "$location", ($scope, ContentService, $location) ->

    $scope.response = {
      kind: "Markdown page"
      settings: {
        showFirst: false
        protect: false
        inTrash: false
        inNews: false
        inIndex: false
        allowResponses: true
        published: Date.now()
      }
      responseTo: $scope.responseTo.id
      tags: {
        nouns: [ "Response" ]
        adjectives: []
        topics: []
      }
      item: {
        text: ""
      }

    }

    $scope.submit = () ->
      ContentService.addContent($scope.responseTo.course, $scope.response).then((resp) ->
        $scope.responseTo.responses.count = $scope.responseTo.responses.count + 1
        $scope.responseTo.responses.entries.push(resp.id)
      )
      $scope.submitted = true

  ]


  Impressory.angularApp.directive("entryAddResponse", () ->
    {
      restrict: 'E'
      controller: Impressory.Controllers.AddContent.EntryAddResponse
      scope: { responseTo: '=' }
      template: """
         <div ng-show="!submitted" class="form form-horizontal" role="form">
            <div >
              <edit-markdown text="response.item.text" prompt="Write a reply"></edit-markdown>
            </div>
            <div>
              <button ng-click="submit()" class="btn btn-primary" ng-show="response.item.text">Submit</button>
            </div>
         </div>
      """
    }
  )

)