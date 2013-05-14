/*
 * Require.js set of modules for various info style pages.
 * (At the moment we're just including everything!)
 */
require([
  "model/base",
  "modules/app",
  "modules/viewingCourse",
  "modules/viewingContent",
  "modules/viewingEvents",
  "modules/viewingUsers",
  
  "controllers/login/IsLoggedIn",
  "controllers/login/LogInForm",
  "controllers/login/SignUp",
  
  "controllers/self/Self",
  
  "controllers/front/ListedCourses",
  "controllers/front/MyCourses",
  
  "controllers/course/Create",
  "controllers/course/Cover",
  "controllers/course/Index",
  
  "controllers/components/EditTags",

  "controllers/viewContent/ViewContent",
  "controllers/viewContent/Layout",
  "controllers/viewContent/TopNav",
  "controllers/viewContent/Sequence",
  "controllers/viewContent/MainContent",
  "controllers/viewContent/EditDetails",
  "controllers/viewContent/ListContentForTopic",
  "controllers/viewContent/IframeSizer",
  
  "controllers/editContent/EditContent",
  
  "controllers/eventRoom/ViewEvents",
  
  "controllers/addContent/TopLevel",
  "controllers/addContent/GenericAddForm",
  "controllers/addContent/WebPage",
  "controllers/addContent/Sequence"
  
], function(l) {
	console.log("library has loaded")
	
	angular.bootstrap(document, ['impressory'])
})