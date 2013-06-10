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
        
        Users: { 
          cache: { }
        }
        
        EventRoom: {
        
          # A list of events to display that have been received
          events: [
            { type: 'chat', text: 'hello there' }
          ]
          
          # Caches the state of various items (usually content entries)
          states: {
          }
        
        }
        
      }
      
    }
    
    EventRoom: null
  
  }
  
  console.log("Impressory base model defined")

)