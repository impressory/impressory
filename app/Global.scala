
import com.impressory.model
import com.impressory.model.CourseModel
import play.api._
import Play.current
import play.api.mvc.AcceptExtractors
import com.impressory.api._
import com.impressory.reactivemongo._
import com.wbillingsley.handy._
import com.impressory.poll.Plugin
import play.api.mvc.Results
import com.impressory.builtincontent.markdown.MarkdownPageModel
import com.impressory.builtincontent.sequence.SequenceModel
import scala.concurrent.Future
import play.api.libs.json.Json
import com.wbillingsley.handyplay._
import com.impressory.plugins.ContentItemViewHandler



object Global extends GlobalSettings with AcceptExtractors {

  def setDefaultContent() {
    /*
     * Set the default content for newly created books and pages
     */
    val source1 = io.Source.fromInputStream(getClass.getResourceAsStream("/defaultMarkdownContent.md"))
    MarkdownPageModel.defaultText = source1.mkString
    source1.close()

    val source2 = io.Source.fromInputStream(getClass.getResourceAsStream("/defaultPageOneContent.md"))
    CourseModel.defaultPageOneText = source2.mkString
    source2.close()
  }
  
  override def onStart(app: Application) {
    
    // Set default content for various content entry kinds
    setDefaultContent()
    
    // Set up the database
    DBConnector.dbName = Play.configuration.getString("mongo.dbname").getOrElse("impressory")
    DBConnector.connectionString = Play.configuration.getString("mongo.connection").getOrElse("localhost:27017")
    DBConnector.dbUser = Play.configuration.getString("mongo.dbuser")
    DBConnector.dbPwd = Play.configuration.getString("mongo.dbpwd")
    
    // Set the execution context (ie the thread pool) that RefFuture work should happen on
    RefFuture.executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

    // Set the completion action for OAuth
    com.wbillingsley.handy.playoauth.PlayAuth.onAuth = com.impressory.auth.controllers.InterstitialController.onOAuth
    
    // Set the home action for DataAction
    com.impressory.plugins.RouteConfig.dataActionConfig = new DataActionConfig {
      def homeAction = com.impressory.play.controllers.Application.index
      
      def errorCodeMap = Map(classOf[UserError] -> 400)
    }
    
    import com.impressory.plugins.LookUps
    import com.impressory.reactivemongo._

    // Register the DAOs
    com.impressory.reactivemongo.Plugin.onStart()

    // Register plugins
    com.impressory.builtincontent.Plugin.onStart()
    com.impressory.external.Plugin.onStart()
    com.impressory.poll.Plugin.onStart()
  }
  
  /**
   * We have many routes that only exist on the client side. 
   */
  override def onHandlerNotFound(request:play.api.mvc.RequestHeader) = {
    request match {
      case Accepts.Html() => {
        val fo = com.impressory.play.controllers.Application.indexInner(request).toFutOpt
        fo.map(_ getOrElse Results.NotFound)(RefFuture.executionContext)
      }
      case Accepts.Json() => Future.successful(Results.NotFound(Json.obj("error" -> "not found")))
      case _ => Future.successful(Results.NotFound)
    }
  }


}