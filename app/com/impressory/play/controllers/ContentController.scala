package com.impressory.play.controllers

import com.wbillingsley.handy._
import Ref._
import com.wbillingsley.handyplay.RefConversions._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.play.model._
import ResultConversions._
import JsonConverters._
import play.api.libs.iteratee.Enumerator

object ContentController extends Controller {
  
  /**
   * Finds an appropriate ContentEntry
   */
  def contentQuery(
    courseId:String,
    entryId:Option[String],
    adj:Option[String], 
    noun:Option[String], 
    topic:Option[String],
    site:Option[String]
  ) = Action { implicit request =>

    val course = RefById(classOf[Course], courseId)
    val user = RequestUtils.loggedInUser(request)
    
    val entry = entryId match {
      // An entry ID has been given. Fetch it if allowed.
      case Some(eid) => {
        val entry = RefById(classOf[ContentEntry], eid)
        
        for (
          u <- optionally(user);
          e <- entry;
          approval = Approval(u);
          a <- approval ask Permissions.ReadEntry(e.itself)
        ) yield e
      }
      
      // An entry ID has not been given. Search according to the filters.
      case None => {
        
        val filters = Map.empty[String, String] ++
          adj.map { a => "adjective" -> a } ++
          noun.map { n => "noun" -> n } ++
          site.map { s => "site" -> s }
          
        for (
          u <- optionally(user);
          c <- course;
          approval = Approval(u);
          e <- ContentModel.recommendCE(c.itself, approval, topic, filters)
        ) yield e
      }
    }
    
    // Find whether to display this entry as part of a ContentSequence
    val result = for (
        eis <- ContentModel.entryInSequence(entry, RefNone);
        j <- eis.itself.toJson
    ) yield Ok(j)
      
    result
  }
  
  
  /**
   * Creates a new content entry and fills it in with the appropriate item.
   * TODO: This is a big ugly method; tidy it up.
   */
  def newContentEntry(course:Ref[Course], user:Ref[User], requestBody:JsValue) = {
    
    import Permissions._
    
    val kind = (requestBody \ "kind").asOpt[String]
    val protect = (requestBody \ "entry" \ "protect").asOpt[Boolean].getOrElse(false)
        
    for (
      // Resolve the references now, to save resolving them multiple times later
      u <- optionally(user);
      c <- course;
      
      // Check the user is allowed to create the content (and to protect it if they've chosen to)
      approval = Approval(u);
      approved <- {
        if (protect) {
          approval ask(AddContent(c.itself), ProtectContent(c.itself))
        } else {
          approval ask AddContent(c.itself) 
        }
      };
      
      // Create a ContentEntry (without its item) from the data
      e <- ContentEntry.unsaved(c.itself, Ref(u), kind);
      updated = ContentModel.update(e, requestBody \ "entry");
      
      // Add an appropriate item
      item <- {
        (requestBody \ "kind").asOpt[String] match {
          case Some(ContentSequence.itemType) => SequenceModel.create(c.itself, approval, updated, requestBody).itself
          case Some(WebPage.itemType) => WebPageModel.create(c.itself, approval, updated, requestBody).itself
          case _ => RefNone
        }        
      };
      
      // Set the item, and save
      saved <- {
        updated.item = Some(item)
        ContentEntry.saveNew(updated)
      };
      
      // Check for any sequences the new entry is part of
      eis <- ContentModel.entryInSequence(saved.itself, RefNone);
      
      // Convert to JSON
      j <- eis.itself.toJson
    ) yield Ok(j)    
  }

  /**
   * Request handler for creating content.
   */
  def createContent(courseId: String) = Action(parse.json) { implicit request =>
    val course = RefById(classOf[Course], courseId)
    val user = RequestUtils.loggedInUser(request)

    val result = newContentEntry(course, user, request.body)
    result
  }

}