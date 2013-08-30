import com.wbillingsley.handy._
import play.api._
import Play.current
import com.impressory.play.model._
import com.impressory.reactivemongo.{ DB, HasBSONId }
import play.api.mvc.AcceptExtractors
import com.wbillingsley.handy.RefFuture

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
    DB.dbName = Play.configuration.getString("mongo.dbname").getOrElse("impressory")
    DB.connectionString = Play.configuration.getString("mongo.connection").getOrElse("localhost:27017")
    DB.dbUser = Play.configuration.getString("mongo.dbuser")
    DB.dbPwd = Play.configuration.getString("mongo.dbpwd")
    
    // Set the execution context (ie the thread pool) that RefFuture work should happen on
    RefFuture.executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

    // Set the completion action for OAuth
    com.wbillingsley.handy.playoauth.PlayAuth.onAuth = com.impressory.auth.controllers.InterstitialController.onOAuth
    
    RefById.lookUpMethod = new RefById.LookUp {
      
      def lookup[T](r: RefById[T, _]) = r.clazz match {
        case c if c == classOf[User] => {
          HasBSONId.GetsBSONId.canonical(r.id) match {
            case Some(id) => User.byId(id).asInstanceOf[Ref[T]]
            case None => RefNone
          }
        }
        case c if c == classOf[Course] => {
          HasBSONId.GetsBSONId.canonical(r.id) match {
            case Some(id) => Course.byId(id).asInstanceOf[Ref[T]]
            case None => RefNone
          }
        }
        case c if c == classOf[ContentEntry] => {
          HasBSONId.GetsBSONId.canonical(r.id) match {
            case Some(id) => ContentEntry.byId(id).asInstanceOf[Ref[T]]
            case None => RefNone
          }
        }
        case c if c == classOf[QnAQuestion] => {
          HasBSONId.GetsBSONId.canonical(r.id) match {
            case Some(id) => QnAQuestion.byId(id).asInstanceOf[Ref[T]]
            case None => RefNone
          }
        }
        case _ => RefFailed(new IllegalArgumentException(s"I don't know how to look up class ${r.clazz.getName}"))
      }
    }
    
    RefManyById.lookUpMethod = new RefManyById.LookUp {
      def lookup[T](r: RefManyById[T, _]) = r.clazz match {
        case c if c == classOf[User] => {
          val ids = for (id <- r.rawIds; c <- HasBSONId.GetsBSONId.canonical(id)) yield c
          User.manyById(ids).asInstanceOf[RefMany[T]]
        }
        case c if c == classOf[Course] => {
          val ids = for (id <- r.rawIds; c <- HasBSONId.GetsBSONId.canonical(id)) yield c
          Course.manyById(ids).asInstanceOf[RefMany[T]]
        }
        case c if c == classOf[ContentEntry] => {
          val ids = for (id <- r.rawIds; c <- HasBSONId.GetsBSONId.canonical(id)) yield c
          ContentEntry.manyById(ids).asInstanceOf[RefMany[T]]
        }
        case _ => RefFailed(new IllegalArgumentException(s"I don't know how to look up class ${r.clazz.getName}"))
        }
    }

  }

  override def onHandlerNotFound(request: play.api.mvc.RequestHeader) = {

    request match {
      // For HTML, return index page so Angular can handle the route
      case Accepts.Html() => {
        import Ref._
        import com.impressory.play.controllers.RequestUtils
        import com.impressory.play.json.JsonConverters._
        import play.api.mvc.Results.{Async, Ok, NotFound, InternalServerError}
        
        val p = scala.concurrent.promise[play.api.mvc.Result]
        val ref = for (
          u <- optionally(RequestUtils.loggedInUser(request.session).toJson)
        ) yield {
          Ok(views.html.main(u))
        }
        ref.onComplete(
          onSuccess = p.success(_), 
          onNone = p.success(NotFound), 
          onFail = p.failure(_) 
        )
        
        Async { p.future }
      }
      case Accepts.Json() => play.api.mvc.Results.NotFound("{ \"error\": \"not found\" }")
      case _ => play.api.mvc.Results.NotFound(views.html.xErrorNotFound("Not found"));
    }

  }

}