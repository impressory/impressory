
#
# Generic connection to the server's EventRoom
#
# (From my repo at github.com/wbillingsley/eventroom-js)

# An EventViewer that connects to the server's EventRoom
class EventViewer

  constructor: (@param) ->
    @sseUrl = window.EventRoom.sseUrl(@param)
    @subscribeUrl = window.EventRoom.subscribeUrl(@param)
    @websocketUrl = window.EventRoom.websocketUrl(@param)
    @pollForEventsUrl = window.EventRoom.pollForEventsUrl(@param)
    
  # Name of this listener on the server
  serverListenerName: null
  
  # Listeners to this EventViewer at the client
  listeners: []
  
  # Whether we have started to establish a connection
  startedConnecting: false
  
  # Whether we have successfully established a connection
  connected: () -> @serverListenerName?
  
  addListener: (l) ->
    @listeners.push(l)
    
  removeListener: (del) ->
    @listeners = (l for l in @listeners when l != del) 
    
  # Callback that receives events
  receive: (msg) ->
    console.log(msg)  
    for l in @listeners
      l.receive(msg)      
  
  # for example:
  # { type: "chatStream", id: bookId }
  # { type: "pollResults", id: pollId }
  # etc
  subscribe: (subscription) ->
    if @connected() 
      data = {
        listenerName : @serverListenerName
        subscription : subscription
      }
      console.log(data)
      jQuery.ajax({
        url: @subscribeUrl
        data: JSON.stringify(data)
        type: 'POST'
        contentType: 'application/json'
      })
      @rememberedSubscriptions.push(subscription)
      null      
      
  # If a subscription comes in while we're still trying to 
  # connect, but before the connection has been established,
  # it gets queued here.
  pendingSubscriptions: []
  
  # Calls subscribe for each of the subscriptions that had to wait.
  doPendingSubscriptions: () ->
    subs = @pendingSubscriptions
    @pendingSubscriptions = []
    for s in subs
      @subscribe(s)
  
  # Connects to the server
  connect: (subscription) -> 
    ev = this    
    if not @connected()
      if @startedConnecting
        # The subscription will have to wait til the connection is established
        # to avoid creating multiple connections
        @pendingSubscriptions.push(subscription)
      else
        if (window.EventSource?)
          @connectSSE(subscription)
        else if (window.WebSocket?)
          @connectWebsocket(subscription)
        else
          #window.alert("This browser doesn't support WebSockets or Server-Sent Events.")
          #@connectPollForEvents(subscription)          
    else
      @subscribe(subscription)
      
  connectSSE: (subscription) -> 
    ev = this    
    url = if subscription?
      @sseUrl + "?subscriptions=" + escape(JSON.stringify(subscription))
    else
      @sseUrl
    console.log("url is " + url)
    console.log("started connecting is " + @startedConnecting)
    @startedConnecting = true
    evtSrc = new EventSource(url)
    evtSrc.onopen = () -> 
      console.log("Connected")
    evtSrc.onmessage = (e) ->
      data = JSON.parse(e.data)
      if data.type == "connected"          
        ev.serverListenerName = data.listenerName
        console.log("Connected as " + ev.serverListenerName)
        ev.doPendingSubscriptions()    
      else 
        ev.receive(data)
    
  connectWebsocket: (subscription) -> 
    ev = this    
    url = if subscription?
      @websocketUrl + "?subscriptions=" + escape(JSON.stringify(subscription))
    else 
      @websocketUrl
    console.log("Websocket URL is " + url)
    console.log("started connecting is " + @startedConnecting)
    @startedConnecting = true
    websocket = new WebSocket(url)
    websocket.onopen = () -> 
      console.log("Connected")
    websocket.onerror = (err) -> console.log("Error: " + err) 
    websocket.onmessage = (e) => 
      console.log("data received")
      console.log(e)
      data = JSON.parse(e.data)
      if data.type == "connected"          
        ev.serverListenerName = data.listenerName
        console.log("Connected as " + ev.serverListenerName)
        ev.doPendingSubscriptions()
      else 
        ev.receive(data)

  # In the polling case, we need to remember the subscriptions to send each time
  rememberedSubscriptions: []
        
  # Although this code is here, it's not recommended to use it yet.
  # The problem is that many subscription requests cause an immediate reply event.
  # This could cause an accidental DDOS, as the reply event would close the connection
  # Causting another connection (including a subscription) request to be made...
  connectPollForEvents: (subscription) ->
    ev = this
    if subscription?
      @rememberedSubscriptions.push(subscription)
        
    url = if @rememberedSubscriptions?.length > 0
      u = @pollForEventsUrl
      sep = "?"
      for s in @rememberedSubscriptions
        u = u + sep + "subscriptions=" + escape(JSON.stringify(s))
        sep = "&"
      u
    else 
      @pollForEventsUrl
    
    console.log("Connecting with URL " +url)
    jQuery.ajax({
      type: "GET"
      dataType: "text"
      url: url
      success: (data) -> 
        eventWrapper = JSON.parse(data.trim())
        console.log(eventWrapper)
        for event in eventWrapper.events
          ev.receive(event)                
        ev.connectPollForEvents()
      error: (jqXHR, status, err) -> 
        console.log("Error from polling for events")
        console.log(err)    
    })

# An EventViewer that delegates to another EventViewer
class DelegatingEventViewer

  constructor: (@delegate) ->
    @delegate.addListener(this)
    dev = this
    $(window).unload(() -> dev.delegate.removeListener(dev))
    
  # Listeners to this EventViewer at the client
  listeners: []
  
  addListener: (l) ->
    @listeners.push(l)
    
  receive: (msg) ->
    for l in @listeners
      l.receive(msg)
      
  connected: () -> @delegate?.connected()
  
  connect: (data) -> @delegate.connect(data)
  
  subscribe: (subscription) -> @delegate.subscribe(subscription)
  

#
# Public global interface to the module
#
window.EventRoom = 
  
  # The URL that will be connected to for Server Sent Events
  sseUrl: (param) -> "/eventRoom/ssevents"
  
  # The URL that will be connected to for Websockets
  websocketUrl: (param) -> "ws:" + window.location.host + "/eventRoom/websocketEvents"

  # The URL that would be connected to for long-polling if it was working!
  pollForEventsUrl: (param) -> "/eventRoom/pollForEvents"

  # The URL that will be posted to to subscribe to new ListenTos
  subscribeUrl: (param) -> "/eventRoom/subscribe"
  
  eventViewer: null
  
  # Creates a new EventRoom. The parameter can be used to distinguish between
  # EventRooms on the same server if you want to have multiple ones open.
  create: (param) ->        
    if not @eventViewer?
      if window.parent? and (window.parent != window)
        if window?.parent?.EventRoom?
          console.log("Intialising parent's event room")
          window.parent.EventRoom.init(param)
        if window?.parent?.EventRoom?.eventViewer? and (window.parent.EventRoom.eventViewer.param == param)
          @eventViewer = new DelegatingEventViewer(window.parent.EventRoom.eventViewer)
        else
          @eventViewer = new EventViewer(param)
      else
        @eventViewer = new EventViewer(param)
    @eventViewer
    
    


# This returns EventRoom as a require.js module
#
try 
  define(() -> window.EventRoom)
catch ex
  console.log("Require.js not present. That's ok")


