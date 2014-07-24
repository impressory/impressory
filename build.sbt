name in ThisBuild := "impressory-play"

organization in ThisBuild := "com.impressory"

scalaVersion in ThisBuild := "2.11.1"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers in ThisBuild  += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers in ThisBuild  += "sonatype snaps" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers in ThisBuild += "typesafe snaps" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers in ThisBuild += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers in ThisBuild += "bintrayW" at "http://dl.bintray.com/wbillingsley/maven"

resolvers in ThisBuild  += DefaultMavenRepository

// Universal dependencies

libraryDependencies in ThisBuild ++= Seq(
  "com.wbillingsley" %% "handy" % "0.6.0-SNAPSHOT",
  "com.wbillingsley" %% "handy-user" % "0.6.0-SNAPSHOT",
  "com.wbillingsley" %% "handy-play" % "0.6.0-SNAPSHOT",
  "com.wbillingsley" %% "eventroom" % "0.2.0-SNAPSHOT"
)

// Define the modules

lazy val api = project in file("modules/api")

lazy val model = project in file("modules/model") dependsOn(api)

lazy val reactivemongo = project in file("modules/reactivemongo") dependsOn(api, model)

lazy val builtincontent = (project in file("modules/builtincontent"))
  .enablePlugins(play.PlayScala)
  .dependsOn(api, model, reactivemongo)

lazy val polls = (project in file("modules/polls"))
  .enablePlugins(play.PlayScala)
  .dependsOn(api, model, reactivemongo)

lazy val externalContent = (project in file("modules/externalcontent"))
  .enablePlugins(play.PlayScala)
  .dependsOn(api, model, reactivemongo)

lazy val main = (project in file("."))
  .enablePlugins(play.PlayScala)
  .settings(
    PlayKeys.routesImport ++= Seq(
      "com.wbillingsley.handy._",
      "com.impressory.api._",
      "com.impressory.play.PathBinders._",
      "scala.language.reflectiveCalls"
    )
  )
  .dependsOn(api, model, reactivemongo, builtincontent, polls, externalContent)

// Dependencies for main

libraryDependencies ++= Seq(
  "com.wbillingsley" %% "handy-play-oauth" % "0.3.0-SNAPSHOT",
  // JavaScript
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "org.webjars" % "font-awesome" % "4.1.0",
  "org.webjars" % "angularjs" % "1.2.20"
)
    

pipelineStages := Seq(rjs)

includeFilter in (Assets, LessKeys.less) := "main.less"
