define(["./app"], () ->

  Impressory.angularApp.service('QnAService', ['$http', ($http) ->
    {
      createQuestion: (courseId, question) -> $http.post("/course/#{courseId}/qna/new", question)
      
      fetchQuestions: (courseId) -> $http.get("/course/#{courseId}/qna")
       
    }
  ])


)