
import play.api._
import Play.current
import play.api.mvc.AcceptExtractors

import com.impressory.api._
import com.impressory.reactivemongo._
import com.wbillingsley.handy._



object Global extends GlobalSettings with AcceptExtractors {

  def setDefaultContent() {
    /*
     * Set the default content for newly created books and pages
     */
    val source1 = io.Source.fromInputStream(getClass.getResourceAsStream("/defaultMarkdownContent.md"))
    com.impressory.play.model.MarkdownPageModel.defaultText = source1.mkString
    source1.close()

    val source2 = io.Source.fromInputStream(getClass.getResourceAsStream("/defaultPageOneContent.md"))
    com.impressory.play.model.CourseModel.defaultPageOneText = source2.mkString
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
    
    RefById.lookUpMethod = new RefById.LookUp {
      val pf = UserDAO.lookupPf orElse
               CourseDAO.lookupPf orElse
               ContentEntryDAO.lookupPf
      
      def lookup[T](r: RefById[T, _]) = pf.apply(r).asInstanceOf[Ref[T]]
    }
    
    RefManyById.lookUpMethod = new RefManyById.LookUp {
      val pf = UserDAO.lookupManyPf orElse
               CourseDAO.lookupManyPf orElse
               ContentEntryDAO.lookupManyPf
      
      def lookup[T](r: RefManyById[T, _]) = pf.apply(r).asInstanceOf[RefMany[T]]
    }
    
    // TODO: Set the home action for Angular

  }


}