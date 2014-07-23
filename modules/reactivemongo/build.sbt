libraryDependencies += "com.wbillingsley" %% "handy-reactivemongo" % "0.6.0-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0")

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.11.0-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0")

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0")

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "org.specs2" %% "specs2" % "2.3.13" % "test"

parallelExecution in Test := false
