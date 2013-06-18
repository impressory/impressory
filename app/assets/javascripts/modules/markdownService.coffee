define(["./app"], () ->

  Impressory.angularApp.service('markdownService', [ '$sanitize', '$compile', ($sanitize, $compile) ->
      
    addExtraMarkup = (conv) ->
      # <<
      # (html)
      # >> 
      # becomes <section>(html)</section>
      conv.hooks.chain("preBlockGamut", (text, runBlockGamut) ->
        text.replace(/^ {0,3}\<\< *\n((?:.*?\n)+?) {0,3}\>\> *$/gm, (whole, inner) ->
          "<section>" + runBlockGamut(inner) + "</section>\n"
        )
      )
    
      # [[
      # (html)
      # ]] 
      # becomes <section>(html)</section>
      conv.hooks.chain("preBlockGamut", (text, runBlockGamut) ->
        text.replace(/^ {0,3}\[\[ *\n((?:.*?\n)+?) {0,3}\]\] *$/gm, (whole, inner) ->
            "<section>" + runBlockGamut(inner) + "</section>\n"
        )
      )
    
      # <reveal<
      # (html)
      # >reveal>
      # becomes <div class="reveal"><div class="slides">(html)</div></div>
      conv.hooks.chain("preBlockGamut", (text, runBlockGamut) ->
        text.replace(/^ {0,3}\<reveal\< *\n((?:.*?\n)+?) {0,3}\>reveal\> *$/gm, (whole, inner) ->
            "<div class=\"reveal\"><div class=\"slides\">" + runBlockGamut(inner) + "</div></div>\n"
        )
      )   
    
      # @poll(pollId)
      # becomes <div data-ib-replace="poll" data-poll-id="pollId">replaceme</div>
      conv.hooks.chain("preBlockGamut", (text, runBlockGamut) ->
        text.replace(/^ {0,3}\@poll\((\w+)\) *$/gm, (whole, inner) ->
            "<div data-ib-replace=\"poll\" data-ib-poll=\"#{inner}\">#{inner}</div>"
        )
      )   
    
      # @pollResults(pollId)
      # becomes <div data-ib-replace="poll" data-poll-id="pollId">replaceme</div>
      conv.hooks.chain("preBlockGamut", (text, runBlockGamut) ->
        text.replace(/^ {0,3}\@pollResults\((\w+)\) *$/gm, (whole, inner) ->
            "<div data-ib-replace=\"pollResults\" data-ib-poll=\"#{inner}\">#{inner}</div>"
        )
      )   
    
      # @textPoll(pollId)
      # becomes <div data-ib-replace="textPoll" data-poll-id="pollId">replaceme</div>
      conv.hooks.chain("preBlockGamut", (text, runBlockGamut) ->
        text.replace(/^ {0,3}\@textPoll\((\w+)\) *$/gm, (whole, inner) ->
            "<div data-ib-replace=\"textPoll\" data-ib-poll=\"#{inner}\">#{inner}</div>"
        )
      )   
    
      # @textPollResults(pollId)
      # becomes <div data-ib-replace="textPoll" data-poll-id="pollId">replaceme</div>
      conv.hooks.chain("preBlockGamut", (text, runBlockGamut) ->
        text.replace(/^ {0,3}\@textPollResults\((\w+)\) *$/gm, (whole, inner) ->
            "<div data-ib-replace=\"textPollResults\" data-ib-poll=\"#{inner}\">#{inner}</div>"
        )
      )   
    
    converter = new Markdown.Converter()
    
        
    postProcess = (text) ->
      text

    {
      makeHtml: (text) ->
        postProcess($sanitize(converter.makeHtml(text)))
        
    
    }
  ])

)