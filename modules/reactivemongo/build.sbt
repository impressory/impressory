libraryDependencies += "com.wbillingsley" %% "handy" % "0.5-SNAPSHOT"

libraryDependencies += "com.wbillingsley" %% "handy-play" % "0.5-SNAPSHOT"

libraryDependencies += "com.wbillingsley" %% "handy-reactivemongo" % "0.5-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0")

libraryDependencies += "com.wbillingsley" %% "salt-encrypt" % "0.1-SNAPSHOT"

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.10.0-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0")

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0")

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "org.specs2" %% "specs2" % "2.2" % "test"

parallelExecution in Test := false
