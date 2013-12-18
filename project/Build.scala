import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  
    val appName         = "impressory"
    val appVersion      = "0.2-SNAPSHOT"
     
    lazy val impressoryApi = Project(appName + "-api", base = file ("modules/api"))
    
    lazy val impressoryModel = Project(appName + "-model", base = file ("modules/model")).dependsOn(impressoryApi)
    
    lazy val impressoryReactivemongo = Project(appName + "-reactivemongo", base = file ("modules/reactivemongo")).dependsOn(impressoryApi)
      
    val appDependencies = Seq(
      "com.wbillingsley" %% "handy" % "0.5-SNAPSHOT",
      "com.wbillingsley" %% "handy-play" % "0.5-SNAPSHOT",
      "com.wbillingsley" %% "eventroom" % "0.1-SNAPSHOT",
      "com.wbillingsley" %% "handy-play-oauth" % "0.2-SNAPSHOT"
    )
    
    lazy val impressoryPolls = play.Project(appName + "-polls", appVersion, appDependencies, path=file("modules/polls")).dependsOn(impressoryApi, impressoryModel, impressoryReactivemongo)
    
    lazy val impressoryExternalContent = play.Project(appName + "-externalcontent", appVersion, appDependencies, path=file("modules/externalcontent")).dependsOn(impressoryApi, impressoryModel, impressoryReactivemongo)

    lazy val mainProj = play.Project(appName, appVersion, appDependencies).settings(

      templatesImport += "_root_.com.wbillingsley.handy._",

      resolvers ++= Seq(
        "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
      ),

      requireJs ++= Seq(
          "main.js" 
      )
    
        // Add your own project settings here      
    ).dependsOn(
      impressoryApi,
      impressoryModel,
      impressoryReactivemongo,
      impressoryExternalContent,
      impressoryPolls
    )
  
    override def rootProject = Some(mainProj)

}
