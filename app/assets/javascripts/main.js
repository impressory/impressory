/*
 * Require.js set of modules for various info style pages.
 * (At the moment we're just including everything!)
 */
require([
  "lib/eventRoom",
         
  "model/base",
  "modules/app",

  "controllers/analytics/base",
  "controllers/analytics/ViewAnalytics",
  
  "controllers/login/IsLoggedIn",
  "controllers/login/LogInForm",
  "controllers/login/SignUp",
  
  "controllers/user/Self",
  "controllers/user/ChangePassword",
  
  "controllers/front/Header",
  "controllers/front/ListedCourses",
  "controllers/front/MyCourses",
  
  "controllers/course/WrapWithChat",
  "controllers/course/Subheader",
  "controllers/course/Create",
  "controllers/course/Edit",
  "controllers/course/Cover",
  "controllers/course/Invites",
  "controllers/course/ActivityStream",
  "controllers/course/Index",
  "controllers/course/MyDrafts",
  
  "controllers/qna/List",
  "controllers/qna/View",
  "controllers/qna/Create",
  
  "controllers/components/EditTags",
  "controllers/components/FillHeight",
  "controllers/components/ListEntries",
  "controllers/components/SceTrustSrc",

  "controllers/viewContent/ViewContent",
  "controllers/viewContent/TopNav",
  "controllers/viewContent/Voting",
  "controllers/viewContent/Comments",
  "controllers/viewContent/Sequence",
  "controllers/viewContent/MainContent",
  "controllers/viewContent/EditDetails",
  "controllers/viewContent/ListContentForTopic",
  
  "controllers/editContent/EditContent",
  "controllers/editContent/Sequence",
  
  "controllers/eventRoom/ViewEvents",
  
  "controllers/addContent/TopLevel",
  "controllers/addContent/GenericAddForm",
  "controllers/addContent/Sequence",
  "controllers/addContent/Share",
  
  "controllers/markdownPage/View",
  "controllers/markdownPage/Edit",
  
  "plugins/mcPoll/plugin"
  
], function(l) {
	console.log("library has loaded")
	
	angular.bootstrap(document, ['impressory'])
}, function(err) {
  console.error("Failed loading required scripts")
  console.error(err)
})