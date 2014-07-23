// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.2")

// Use the require.js minifier
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

// Use CoffeeScript
addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

// Use Less CSS
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

// Use Twirl for compiling Angular templates together
addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.2")