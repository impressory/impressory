define(["lib/eventRoom"], (er) ->

  #
  # Sets up the core structure that other parts of the model etc fill out.
  #
  window.Impressory = {
  
    Controllers: {
    }
    
    Model: {
    
      Login: { 
        type: if LoggedInUser? then "user" else "none"
        user: LoggedInUser ? null
        
        login: (user) ->
          @type = "user"
          @user = user
        
        logout: () ->
          @type = "none"
          @user = null
      }
      
      Viewing: {
      
        Course: { }
        
        Content: { }
      
      }
    }
    
    #EventRoom: EventRoom.create()
  
  }
  
  console.log("Impressory base model defined")

)