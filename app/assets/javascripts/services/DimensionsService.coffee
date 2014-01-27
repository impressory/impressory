define(["modules/base"], () ->

  # Forces a digest if the window dimensions change
  Impressory.angularApp.service('DimensionsService',  ['$window', '$rootScope', ($window, $rootScope) ->
      
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