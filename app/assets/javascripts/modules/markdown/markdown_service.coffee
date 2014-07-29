define ['angular', '../app'], () ->

  angular.module("impressory").factory("MarkdownService", () ->

    marked.setOptions({
      renderer: new marked.Renderer(),
      gfm: true,
      tables: true,
      breaks: false,
      pedantic: false,
      sanitize: false,
      smartLists: true,
      smartypants: false
    });

    {
      makeHtml: (text) -> @render(text)

      render: (text) ->
        if text?
          marked(text)
        else
          null
    }

  )