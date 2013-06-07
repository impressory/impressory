/*
 * Require.js set of modules for various info style pages.
 * (At the moment we're just including everything!)
 */
require([
  "lib/eventRoom",
         
  "model/base",
  "modules/app",
  "modules/viewingCourse",
  "modules/viewingContent",
  "modules/viewingEvents",
  "modules/viewingUsers",
  "modules/markdownService",
  
  "controllers/login/IsLoggedIn",
  "controllers/login/LogInForm",
  "controllers/login/SignUp",
  
  "controllers/self/Self",
  
  "controllers/front/ListedCourses",
  "controllers/front/MyCourses",
  
  "controllers/course/Create",
  "controllers/course/Cover",
  "controllers/course/Index",
  "controllers/course/Invites",
  
  "controllers/qna/List",
  "controllers/qna/View",
  "controllers/qna/Create",
  
  "controllers/components/EditTags",
  "controllers/components/FillHeight",

  "controllers/viewContent/ViewContent",
  "controllers/viewContent/Layout",
  "controllers/viewContent/TopNav",
  "controllers/viewContent/Sequence",
  "controllers/viewContent/MainContent",
  "controllers/viewContent/EditDetails",
  "controllers/viewContent/ListContentForTopic",
  
  "controllers/editContent/EditContent",
  
  "controllers/eventRoom/ViewEvents",
  
  "controllers/addContent/TopLevel",
  "controllers/addContent/GenericAddForm",
  "controllers/addContent/WebPage",
  "controllers/addContent/Sequence",
  
  "controllers/markdownPage/View",
  "controllers/markdownPage/Edit",
  
  "controllers/mcPoll/View"
  
], function(l) {
	console.log("library has loaded")
	
	angular.bootstrap(document, ['impressory'])
})