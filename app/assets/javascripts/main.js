/*
 * Require.js set of modules for various info style pages.
 * (At the moment we're just including everything!)
 */
require([
  "lib/eventRoom",
         
  "model/base",
  "modules/app",
  
  "controllers/login/IsLoggedIn",
  "controllers/login/LogInForm",
  "controllers/login/SignUp",
  
  "controllers/self/Self",
  
  "controllers/front/Header",
  "controllers/front/ListedCourses",
  "controllers/front/MyCourses",
  
  "controllers/course/Subheader",
  "controllers/course/Create",
  "controllers/course/Edit",
  "controllers/course/Cover",
  "controllers/course/Index",
  "controllers/course/Invites",
  "controllers/course/ActivityStream",
  
  "controllers/qna/List",
  "controllers/qna/View",
  "controllers/qna/Create",
  
  "controllers/components/EditTags",
  "controllers/components/FillHeight",
  "controllers/components/ListEntries",

  "controllers/viewContent/ViewContent",
  "controllers/viewContent/Layout",
  "controllers/viewContent/TopNav",
  "controllers/viewContent/Voting",
  "controllers/viewContent/Comments",
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
  "controllers/addContent/Share",
  
  "controllers/markdownPage/View",
  "controllers/markdownPage/Edit",
  
  "controllers/mcPoll/View",
  "controllers/mcPoll/Edit"
  
], function(l) {
	console.log("library has loaded")
	
	angular.bootstrap(document, ['impressory'])
})