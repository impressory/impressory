scalaVersion := "2.10.0"

libraryDependencies += "com.wbillingsley" %% "handy" % "0.4-SNAPSHOT"

libraryDependencies += "com.wbillingsley" %% "handy-play" % "0.4-SNAPSHOT"

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.9"

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.9"

libraryDependencies += "com.wbillingsley" %% "salt-encrypt" % "0.1-SNAPSHOT"

libraryDependencies <+= scalaVersion(sv => "org.scala-lang" % "scala-reflect" % sv)
