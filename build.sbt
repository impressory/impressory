name in ThisBuild := "impressory-play"

organization in ThisBuild := "com.impressory"

scalaVersion in ThisBuild := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers in ThisBuild  += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers in ThisBuild  += "sonatype snaps" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers in ThisBuild += "typesafe snaps" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers in ThisBuild += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers in ThisBuild  += DefaultMavenRepository

