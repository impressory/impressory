name in ThisBuild := "impressory-play"

organization in ThisBuild := "com.impressory"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += DefaultMavenRepository

resolvers += JavaNet1Repository

resolvers += "repo.novus rels" at "http://repo.novus.com/releases/"

resolvers += "repo.novus snaps" at "http://repo.novus.com/snapshots/"

resolvers += "typesafe snaps" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "sonatype snaps" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "handy" at "https://bitbucket.org/wbillingsley/mavenrepo/raw/master/snapshots/"

