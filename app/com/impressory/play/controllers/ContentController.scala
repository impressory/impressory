package com.impressory.play.controllers

import com.impressory.model.ContentModel
import com.wbillingsley.handy._
import Ref._
import Id._
import com.wbillingsley.handyplay._
import com.wbillingsley.handyplay.RefConversions._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.impressory.api._
import com.impressory.api.events._
import com.impressory.json._
import com.impressory.plugins._
import com.impressory.eventroom.EventRoom
import com.impressory.model._
import com.impressory.security.Permissions

import com.impressory.reactivemongo.ContentEntryDAO

object ContentController extends Controller {
  
  implicit val etoj = com.impressory.json.ContentEntryToJson
  implicit val eistoj = com.impressory.json.EntryInSequenceToJson
  implicit val cetoj = com.impressory.json.ContentEditedToJson
  implicit val cptoj = com.impressory.json.ContentPublishedToJson
  
  import com.impressory.plugins.LookUps._
  import com.impressory.plugins.RouteConfig._
  
  /**
   * Returns the JSON for the specified content entry
   */
  def entry(rCourse:Ref[Course], rEntry:Ref[ContentEntry]) = DataAction.returning.one { implicit request =>
    for (
        entry <- rEntry;
        approved <- request.approval ask Permissions.readEntry(entry.itself)
    ) yield entry
  }
  
  def findEntriesById(rCourse:Ref[Course]) = DataAction.returning.many(parse.json) { implicit request =>
    val ids = (request.body \ "ids").asOpt[Set[String]].getOrElse(Set.empty)
    for {
      approved <- request.approval ask Permissions.readCourse(rCourse)
      e <- RefManyById.of[ContentEntry](ids.toSeq)
    } yield e
  }  
  
  /**
   * Finds an appropriate ContentEntry
   */
  def contentQuery(
    rCourse:Ref[Course],
    entryId:Option[String],
    adj:Option[String], 
    noun:Option[String], 
    topic:Option[String],
    site:Option[String]
  ) = DataAction.returning.one { implicit request =>

    val course = rCourse
    val approval = request.approval
    
    for {
      e <- entryId match {
        case Some(eid) => {
          // An entry ID has been given. Fetch it if allowed.          
          val entry = eid.asId[ContentEntry].lazily
          for (e <- entry; a <- approval ask Permissions.readEntry(e.itself)) yield e
        }
        case None => {
          // An entry ID has not been given. Search according to the filters.
          val filters = Map.empty[String, String] ++
            adj.map { a => "adjective" -> a } ++
            noun.map { n => "noun" -> n } ++
            site.map { s => "site" -> s }
            for (c <- course; e <- ContentModel.recommendCE(c.itself, approval, topic, filters)) yield e
        }
      }
      
      // Find whether to display this entry as part of a ContentSequence
      eis <- ContentModel.entryInSequence(e.itself, RefNone)
    } yield eis
  }
  
  
  /**
   * Creates a new content entry and fills it in with the appropriate item.
   * TODO: This is a big ugly method; tidy it up.
   */
  def newContentEntry(course:Ref[Course], approval:Approval[User], requestBody:JsValue) = {
    
    import Permissions._

    val protect = (requestBody \ "entry" \ "settings" \ "protect").asOpt[Boolean].getOrElse(false)

    for {
      user <- approval.who

      kind <- (requestBody \ "kind").asOpt[String].toRef orIfNone UserError("Attempted to create a content item with no kind")
        
      // Resolve the references now, to save resolving them multiple times later
      c <- course;
      
      // Check the user is allowed to create the content (and to protect it if they've chosen to)
      approved <- {
        if (protect) {
          approval ask(addContent(c.itself), protectContent(c.itself))
        } else {
          approval ask addContent(c.itself)
        }
      };
      
      // Create a ContentEntry (without its item) from the data
      blank = ContentEntry(
        id=LookUps.allocateId,
        course=c.id, addedBy=user.id, settings=CESettings(protect=protect)
      );
      
      // Updated the metadata, as some settings might be changed (eg, published)
      metaUpdated = ContentEntryToJson.update(blank, requestBody);   
      
      updated <- ContentItemToJson.createFromJson(kind, metaUpdated, requestBody)
      
      // Set the item, and save
      saved <- ContentEntryDAO.saveNew(updated)

      repliedTo <- optionally({
        for {
          rToId <- saved.responseTo.toRef
          rTo <- ContentEntryDAO.addResponse(rToId, saved.id)
        } yield rTo
      })
      
      // Check for any sequences the new entry is part of
      eis <- ContentModel.entryInSequence(saved.itself, RefNone)
    } yield {
      
      // If the item is published, send a notification
      if (saved.settings.published.isDefined) {
        for (c <- course.refId) {
          EventRoom.notifyEventRoom(BroadcastUnique(c, ContentPublished(saved)))
        }
      }

      // If the item is a reply, send an update notification on the repliedTo
      if (repliedTo.isDefined) {
        for (c <- course.refId) {
          EventRoom.notifyEventRoom(BroadcastUnique(c, ContentEdited(repliedTo.get)))
        }
      }


      saved
    }
  }

  /**
   * Request handler for creating content.
   */
  def createContent(rCourse: Ref[Course]) = DataAction.returning.one(parse.json) { implicit request =>
    newContentEntry(rCourse, request.approval, request.body)
  }
  
  def editItem(rCourse: Ref[Course], rEntry:Ref[ContentEntry]) = DataAction.returning.one(parse.json) { implicit request =>
    val approval = request.approval
    
    val r = for {
      // Resolve the references now, to save resolving them multiple times later
      e <- rEntry;
      
      // Was it published before we started?
      publishedBefore = e.settings.published.isDefined;
      
      // Check the user is allowed to create the content (and to protect it if they've chosen to)
      approved <- approval ask Permissions.editContent(e.itself);
      
      // Updated the metadata, as some settings might be changed (eg, published)
      metaUpdated = ContentEntryToJson.update(e, request.body);      
      
      // Edit the item
      updated <- ContentItemToJson.updateFromJson(metaUpdated, request.body)
      
      saved <- ContentEntryDAO.saveWithItem(updated)
    } yield {
      // If the item is published, send a notification
      if (!publishedBefore && saved.settings.published.isDefined) {
        EventRoom.notifyEventRoom(BroadcastUnique(saved.course, ContentPublished(saved)))
      }
      
      saved
    }
    r
  }
  
  def editTags(rCourse:Ref[Course], rEntry:Ref[ContentEntry]) = DataAction.returning.one(parse.json) { implicit request =>
    val approval = request.approval    
    for (
      e <- rEntry;
      approved <- approval ask Permissions.editContent(e.itself);
      updated = ContentEntryToJson.update(e, request.body);
      saved <- ContentEntryDAO.saveExisting(updated)
    ) yield saved
  }
  
  def entriesForTopic(rCourse:Ref[Course], topic:Option[String]) = DataAction.returning.many { implicit request =>
    for (
      c <- rCourse;
      e <- ContentModel.entriesForTopic(c.itself, request.approval, topic)
    ) yield e
  }

  def allEntries(rCourse:Ref[Course]) = DataAction.returning.many { implicit request =>
    for (
      c <- rCourse;
      e <- ContentModel.allEntries(c.itself, request.approval)
    ) yield e
  }  
  
  
  def recentEntries(rCourse:Ref[Course]) = DataAction.returning.many { implicit request =>
    ContentModel.recentEntries(rCourse, request.approval)
  }
  
  def myDrafts(rCourse:Ref[Course]) = DataAction.returning.many { implicit request =>
    ContentEntryDAO.myDrafts(request.user, rCourse)
  }
  
  /**
   * Votes an entry up. Returns JSON for the updated content entry
   */
  def voteUp(rCourse:Ref[Course], rEntry:Ref[ContentEntry]) = DataAction.returning.one { implicit request =>
    for {
      entry <- rEntry;
      user <- request.user
      approved <- request.approval ask Permissions.voteOnEntry(entry.itself);
      updated <- ContentEntryDAO.voteUp(entry, user.itself)
    } yield updated
  }
  
  /**
   * Votes an entry down. Returns JSON for the updated content entry
   */
  def voteDown(rCourse:Ref[Course], rEntry:Ref[ContentEntry]) = DataAction.returning.one { implicit request =>
    for {
      entry <- rEntry;
      user <- request.user
      approved <- request.approval ask Permissions.voteOnEntry(entry.itself);
      updated <- ContentEntryDAO.voteDown(entry, user.itself)
    } yield updated
  }
  
  def addComment(courseId:Ref[Course], rEntry:Ref[ContentEntry]) = DataAction.returning.one(parse.json) { implicit request =>
    for {
      text <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("The message contained no text");
      entry <- rEntry;
      user <- request.user
      approved <- request.approval ask Permissions.commentOnEntry(entry.itself);
      updated <- ContentEntryDAO.addComment(entry, user.id, text)
    } yield updated
  }
  
  /**
   * Given a URL or Embed code, works out what kind of content item it is
   */
  def whatIsIt(code:String) = DataAction.returning.result { implicit request => 
    
    // This prevents the URL from being used to look up itself recursively
    request match {
      case Accepts.Json() => {
        val res = for (
          ce <- ContentItemToJson.whatIsIt(
              // TODO: avoid putting fake IDs into the object
              ContentEntry(id=LookUps.allocateId, addedBy = "".asId[User], course="".asId[Course]),
              code
            ) orIfNone UserError("Sorry, I don't recognise that content");
          j <- ContentEntryToJson.toJsonForInput(ce)
        ) yield Ok(j)
        res
      }
      case _ => NotAcceptable.itself
    }
    
    
    
  }
  
}