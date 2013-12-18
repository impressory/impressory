define(["./base", "services/ContentService"], () ->


  # viewingContent is responsible for maintaining what is shown in the content viewer
  # In other words, it maintains Impressory.Model.Viewing.Content
  #
  #
  Impressory.angularApp.service('viewingContent', ['$location', '$rootScope', '$window', ($location, $rootScope, $window) ->
      
    $(window).on('resize', () ->
      $rootScope.$apply(() -> $rootScope.wh = $window.innerHeight)
    )

    $rootScope.wh = $window.innerHeight    
   
    {
      refreshHeight: () -> 
        $rootScope.wh = $window.innerHeight
    }
  ])


)