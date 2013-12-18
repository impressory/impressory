define(["lib/eventRoom"], (er) ->

  #
  # Sets up the core structure that other parts of the model etc fill out.
  #
  window.Impressory = {
  
    Controllers: {
    }
    
    Services: {
    }
    
    Caches: {
    
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
        
        Content: { 
          # The entry in MainContent
          entry: null
          
          # Instructs Sequence to turn to a particular entry in the sequence
          goToSeqIndex: -1
          
          # The entry Sequence has turned to
          seqEntry: null
        }
        
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