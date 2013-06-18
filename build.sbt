name in ThisBuild := "impressory-play"

organization in ThisBuild := "com.impressory"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers in ThisBuild  += DefaultMavenRepository

resolvers in ThisBuild  += JavaNet1Repository

resolvers in ThisBuild  += "repo.novus rels" at "http://repo.novus.com/releases/"

resolvers in ThisBuild  += "repo.novus snaps" at "http://repo.novus.com/snapshots/"

resolvers in ThisBuild += "typesafe snaps" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers in ThisBuild += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers in ThisBuild  += "sonatype snaps" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers in ThisBuild  += "handy" at "https://bitbucket.org/wbillingsley/mavenrepo/raw/master/snapshots/"

