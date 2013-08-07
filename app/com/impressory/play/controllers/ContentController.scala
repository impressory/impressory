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
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Enumeratee
import com.impressory.play.json.JsonConverters
import JsonConverters._
import com.impressory.play.eventroom.{ EventRoom, ChatEvents, ContentEvents } 

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

    val course = refCourse(courseId)
    val approval = request.approval
    
    val result = for (
      e <- entryId match {
        case Some(eid) => {
          // An entry ID has been given. Fetch it if allowed.          
          val entry = refContentEntry(eid)
          for (e <- entry; a <- approval ask Permissions.ReadEntry(e.itself)) yield e
        }
        case None => {
          // An entry ID has not been given. Search according to the filters.
          val filters = Map.empty[String, String] ++
            adj.map { a => "adjective" -> a } ++
            noun.map { n => "noun" -> n } ++
            site.map { s => "site" -> s }
            for (c <- course; e <- ContentModel.recommendCE(c.itself, approval, topic, filters)) yield e
        }
      };
      
      // Find whether to display this entry as part of a ContentSequence
      eis <- ContentModel.entryInSequence(e.itself, RefNone);
      j <- eis.itself.toJsonFor(approval)
    ) yield Ok(j)
      
    result
  }
  
  
  /**
   * Creates a new content entry and fills it in with the appropriate item.
   * TODO: This is a big ugly method; tidy it up.
   */
  def newContentEntry(course:Ref[Course], approval:Approval[User], requestBody:JsValue) = {
    
    import Permissions._
    
    val kind = (requestBody \ "kind").asOpt[String]
    val protect = (requestBody \ "entry" \ "settings" \ "protect").asOpt[Boolean].getOrElse(false)

    
    for (
      // Resolve the references now, to save resolving them multiple times later
      c <- course;
      
      // Check the user is allowed to create the content (and to protect it if they've chosen to)
      approved <- {
        if (protect) {
          approval ask(AddContent(c.itself), ProtectContent(c.itself))
        } else {
          approval ask AddContent(c.itself) 
        }
      };
      
      // Create a ContentEntry (without its item) from the data
      e <- ContentEntry.unsaved(c.itself, approval.who);
      updated = ContentModel.update(e, requestBody);
      
      // Add an appropriate item
      item <- {
        (requestBody \ "kind").asOpt[String] match {
          case Some(ContentSequence.itemType) => SequenceModel.create(c.itself, approval, updated, requestBody).itself
          case Some(WebPage.itemType) => WebPageModel.create(c.itself, approval, updated, requestBody).itself
          case Some(GoogleSlides.itemType) => OtherExternalsModel.createGoogleSlides(c.itself, approval, updated, requestBody).itself
          case Some(YouTubeVideo.itemType) => OtherExternalsModel.createYouTubeVideo(c.itself, approval, updated, requestBody).itself
          case Some(MarkdownPage.itemType) => MarkdownPageModel.create(c.itself, approval, updated, requestBody)
          case Some(MultipleChoicePoll.itemType) => MCPollModel.create(c.itself, approval, updated, requestBody)
          case Some(x) => RefFailed(UserError(s"Attempted to add an unknown item: $x"))
          case None => RefFailed(UserError("Attempted to add an item that had no kind"))
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
      j <- eis.itself.toJsonFor(approval)
    ) yield {
      
      // If the item is published, send a notification
      if (saved.published.isDefined) {
        for (c <- course.getId; cstr = c.stringify) {
          EventRoom.notifyEventRoom(ChatEvents.BroadcastIt(cstr, ContentEvents.ContentPublished(saved)))
        }
      }
      
      Ok(j)    
    }
  }

  /**
   * Request handler for creating content.
   */
  def createContent(courseId: String) = Action(parse.json) { implicit request =>
    val result = newContentEntry(refCourse(courseId), request.approval, request.body)
    result
  }
  
  def editItem(courseId: String, entryId: String) = Action(parse.json) { implicit request => 
    val entry = refContentEntry(entryId)
    val approval = request.approval
    
    val r = for (
      // Resolve the references now, to save resolving them multiple times later
      e <- entry;
      
      // Was it published before we started?
      publishedBefore = e.published.isDefined;
      
      // Check the user is allowed to create the content (and to protect it if they've chosen to)
      approved <- approval ask Permissions.EditContent(e.itself);
      
      // Updated the metadata, as some settings might be changed (eg, published)
      metaUpdated = ContentModel.update(e, request.body);      
      
      // Edit the item
      updated <- {
        metaUpdated.item match {
          case Some(cs:ContentSequence) => SequenceModel.updateItem(metaUpdated, request.body)
          case Some(wp:WebPage) => WebPageModel.updateWebPage(metaUpdated, request.body)
          case Some(y:YouTubeVideo) => { metaUpdated.item = Some(OtherExternalsModel.updateYouTubeVideo(y,request.body)); metaUpdated.itself }
          case Some(gs:GoogleSlides) => { metaUpdated.item = Some(OtherExternalsModel.updateGoogleSlides(gs,request.body)); metaUpdated.itself }
          case Some(mp:MarkdownPage) => MarkdownPageModel.updateItem(metaUpdated, request.body)
          case Some(p:MultipleChoicePoll) => MCPollModel.updateMCPoll(metaUpdated, request.body)
          case _ => RefNone
        }        
      };
      
      saved <- ContentEntry.saveWithItem(updated);
      
      // Convert to JSON
      j <- saved.toJsonFor(approval)
    ) yield 
    {
      // If the item is published, send a notification
      if (!publishedBefore && saved.published.isDefined) {
        for (c <- saved.course.getId; cstr = c.stringify) {
          EventRoom.notifyEventRoom(ChatEvents.BroadcastIt(cstr, ContentEvents.ContentPublished(saved)))
        }
      }
      
      Ok(j)
    }
    r
  }
  
  def editTags(courseId: String, entryId: String) = Action(parse.json) { implicit request => 
    val approval = request.approval
    
    val r = for (
      e <- refContentEntry(entryId);
      approved <- approval ask Permissions.EditContent(e.itself);
      updated = ContentModel.update(e, request.body);
      saved <- ContentEntry.saveExisting(updated);
      json <- saved.toJsonFor(approval)
    ) yield {
      Ok(json)
    }
    r
  
  }
  
  def entriesForTopic(courseId: String, topic:Option[String]) = Action { implicit request => 

    val entries = for (
      c <- refCourse(courseId);
      e <- ContentModel.entriesForTopic(c.itself, request.approval, topic);
      j <- e.toJson
    ) yield j
      
    val en = Enumerator("{ \"entries\": [") andThen entries.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")    
  }

  def allEntries(courseId: String) = Action { implicit request => 

    val entries = for (
      c <- refCourse(courseId);
      e <- ContentModel.allEntries(c.itself, request.approval);
      j <- e.toJson
    ) yield j
      
    val en = Enumerator("{ \"entries\": [") andThen entries.enumerate.stringify andThen Enumerator("]}") andThen Enumerator.eof[String]
    Ok.stream(en).as("application/json")    
  }  
  
  
  def recentEntries(courseId: String) = Angular { Action { implicit request => 
    val approval = request.approval
    val entries = for (
      c <- refCourse(courseId);
      e <- ContentModel.recentEntries(c.itself, approval);
      j <- e.toJsonFor(approval)
    ) yield j
      
    val r = entries.enumerate &> Enumeratee.take(100)
    enumJToResult(r)
  }}
  
  
  /**
   * Votes an entry up. Returns JSON for the updated content entry
   */
  def voteUp(courseId:String, entryId:String) = Action { implicit request =>
    val approval = request.approval
    val res = for (
      entry <- refContentEntry(entryId);
      approved <- approval ask Permissions.VoteOnEntry(entry.itself);
      updated <- ContentEntry.voteUp(entry, approval.who);
      j <- updated.toJsonFor(approval)
    ) yield j
    res
  }
  
  /**
   * Votes an entry down. Returns JSON for the updated content entry
   */
  def voteDown(courseId:String, entryId:String) = Action { implicit request =>
    val approval = request.approval
    val res = for (
      entry <- refContentEntry(entryId);
      approved <- approval ask Permissions.VoteOnEntry(entry.itself);
      updated <- ContentEntry.voteDown(entry, approval.who);
      j <- updated.toJsonFor(approval)
    ) yield j
    res
  }
  
  def addComment(courseId:String, entryId:String) = Action(parse.json) { implicit request => 
    val approval = request.approval
    val res = for (
      text <- Ref((request.body \ "text").asOpt[String]) orIfNone UserError("The message contained no text");
      entry <- refContentEntry(entryId);
      approved <- approval ask Permissions.CommentOnEntry(entry.itself);
      updated <- ContentEntry.addComment(entry, approval.who, text);
      j <- updated.toJsonFor(approval)
    ) yield j
    res    
  }
  
  /**
   * Given a URL or Embed code, works out what kind of content item it is
   */
  def whatIsIt(code:String) = Action { implicit request => 
    
    // This prevents the URL from being used to look up itself recursively
    request match {
      case Accepts.Json() => {
        import com.impressory.play.json.ContentEntryToJson
        
        val res = for (
          ce <- ContentTypeListing.whatIsIt(code) orIfNone UserError("Sorry, I don't recognise that content");
          j <- ContentEntryToJson.toJsonForInput(ce)
        ) yield j
        res
      }
      case _ => NotAcceptable
    }
    
    
    
  }
  
}