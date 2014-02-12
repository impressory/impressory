Impressory
==========

Impressory is an open source collaborative and interactive learning space with the following goals:

* Provide a place where students and staff can share content and discussion.
* Support live interaction, both online and in the physical classroom, to break down the technological divide between class-time and non-class-time.
* Support groups and group work
* Plug into learning management systems, such as edX or Blackboard, via LTI. As well as supporting social logins such as Twitter and Google Plus.
* Support push-through publishing to Facebook pages, Twitter, and other social venues so that students receive updates where they regularly visit
* CORS support and embedding so that content and interactivity from a course can be included on external sites (such as if you want to design a very custom interactive course page)
* Support data-driven sharing of content between courses
* High performance, and easy scalability, from a small core codebase.

For the more pedagogically minded among you, it's a "connectivist" plugin for your learning environment.


How does it look?
-----------------

Online, students can interact with content in a filterable activity stream, that students should be familiar with from social media. Live interaction is also supported, including chat, polls, and questions.

[![Newsfeed](http://farm8.staticflickr.com/7291/11680625563_25c6e3c8a3_c.jpg)](http://www.flickr.com/photos/13074671@N00/11680625563)

You can also display content in a full screen view, with the same live interactivity. In on-campus courses, we find it useful to give lectures directly from Impressory using the full-screen view. (Don't worry, you can close the chat sidebar if you don't want student messages going up on the screen, though we find it very useful).

[![Viewer](http://farm4.staticflickr.com/3726/11680379025_d00b86a826_c.jpg)](http://www.flickr.com/photos/13074671@N00/11680379025)

In this way, the conversation from the classroom can continue seamlessly out-of-class.


License
-------

Impressory is open source under the [MIT Licence](http://opensource.org/licenses/MIT)


What's its status?
------------------

It's been used on-campus with a small number of courses at the University of Queensland.

We're now building it out to support a collaborative MOOC. That includes filling in missing features, and turning it into a proper open source project.


How do I get it?
----------------

There are a few different ways you can get it up and running. See the [chapter in the documentation](http://impressory-for-users.readthedocs.org/en/latest/setup/index.html) for details

How does it work?
-----------------

We're building out two sets of documentation:

* [Developer documentation](http://impressory-for-developers.readthedocs.org/)
* [User and deployment documentation](http://impressory-for-users.readthedocs.org/)


Who's developing it?
--------------------

It's being developed at [NICTA](http://nicta.com.au) to support a very collaborative software studio course.  NICTA is an Australian computer science research centre that also has an education mission and links to several universities.

The software has tried out and iterated with classes at The University of Queensland.

And while the code is modern, the project has its origins in the Intelligent Book project, which was a collaboration between the University of Cambridge and MIT.


How can I get involved?
-----------------------

There are many ways you could help:

* Being an early adopter, trying it out in your course and giving us feedback on how best to make it serve your needs
* Telling your friends and colleagues about it
* Writing [code](http://github.com/impressory/impressory) or documentation.
* Feel very free to fork it. Make it do what you need, and then ideally share the changes back again.

We'd very much like to make Impressory integrate well with the other technologies that courses use. We're aiming to make it work well with edX, Blackboard, Canvas, and all the other technologies that courses find helpful.

As a public sector organsation with a science and education mission, we're not trying to take over the world, just make it better.


What's the technology stack
---------------------------

We're trying to keep this well modularised, but at the moment the stack is:

* MongoDB storage
* Scala code on the server, making extensive use of [handy](http://github.com/wbillingsley) to give us a degree of independence from the database and web framework, and help us write the functionality in a clear, concise, and parallelisable manner.
* [Play](http://playframework.com) web framework
* [Angular.js](http://angularjs.org) in the Browser


How does that stack help us?
----------------------------

MongoDB gives us a simple document-oriented storage model. Content in a course is heterogeneous (there's videos, articles, quizzes, external embeds, polls, etc), but forms part of a single collection -- the course content. A document-oriented storage model makes this very simple to deal with.

Handy is a library based around a "monad for the web". It helps the code to be concise, functional, safe to parallelise, handles asynchronous calls without requiring complicated code, and provides an easy separation-of-concerns.

Play is an asynchronous, "reactive", and mostly stateless web framework. This makes both handling large loads on a single machine and also scaling to multiple machines easier.

Angular.js provides makes interactivity in the browser clean and straightforward, and also reduces the load on the server significantly. As the user navigates around the course, content is cached and rendered in the browser. This means the browser does not have to re-fetch state (such as the chat stream, or even content the user has seen in this visit), which means fewer requests to the server, which makes the site more responsive to the user, and also reduces the load on the server.



best regards,
Will Billingsley,
NICTA

@wbillingsley
William.Billingsley at NICTA.com.au
