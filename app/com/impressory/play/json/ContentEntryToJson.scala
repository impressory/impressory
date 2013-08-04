package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import play.api.libs.json._

import JsonConverters._


object ContentEntryToJson {
  
  implicit val settingsFormat = Json.format[CESettings]
  
  implicit val tagsFormat = Json.format[CETags]
  
    /**
     * Basic core JSON other methods add to
     */
    def toJsonCore(ce:ContentEntry) = {
      
      Json.obj(
        "id" -> ce.id.stringify,
        "course" -> ce.course,
        "settings" -> ce.settings,
        "voting" -> UpDownVotingToJson.toJson(ce.voting),
        "commentCount" -> ce.commentCount,
        "title" -> ce.title,
        "note" -> ce.note,
        "kind" -> Json.toJson(ce.kind),
        "tags" -> ce.tags,
        "published" -> ce.published,
        "updated" -> ce.updated,
        "created" -> ce.created,
        "addedBy" -> ce.addedBy
      )
    }    
    
    /**
     * JSON for a course, without permissions etc
     */
    def toJson(ce:ContentEntry):Ref[JsObject] = {
            
      for (
        item <- Ref(ce.item);
        itemj <- item.toJson
      ) yield {
        toJsonCore(ce) ++ Json.obj("item" -> itemj)
      }
    }
    
    /**
     * Permissions on the content entry
     */
    def permissions(ce:ContentEntry, appr:Approval[User]) = for (
       add <- optionally(appr ask Permissions.AddContent(ce.course));
       read <- optionally(appr ask Permissions.ReadEntry(ce.itself));
       edit <- optionally(appr ask Permissions.EditContent(ce.itself));
       vote <- optionally(appr ask Permissions.VoteOnEntry(ce.itself));
       comment <- optionally(appr ask Permissions.CommentOnEntry(ce.itself))
    ) yield Json.obj(
      "add" -> add.isDefined,
      "read" -> read.isDefined,
      "edit" -> edit.isDefined,
      "vote" -> vote.isDefined,
      "comment" -> comment.isDefined
    )
    
    /**
     * JSON for a Course, including permissions 
     * information for this User.
     */
    def toJsonFor(ce:ContentEntry, appr:Approval[User]):Ref[JsObject] = {
      // Combine the JSON responses, noting that reg or perms might be RefNone
      for (
        ej <- toJson(ce);
        p <- optionally(permissions(ce, appr));
        item <- Ref(ce.item);
        itemj <- item.toJsonFor(appr);
        voting <- ce.voting.toJsonFor(appr);
        comments <- new RefTraversableOnce(ce.comments).toJsonFor(appr)
      ) yield ej ++ Json.obj(
          "permissions" -> p,
          "item" -> itemj,
          "voting" -> voting,
          "comments" -> comments
      )
    }  
    
    /**
     * Cut-down JSON, that would be acceptable being received from the client.
     * Used by ContentController.whatIsIt
     */
    def toJsonForInput(ce:ContentEntry) = {      
      for (
        item <- Ref(ce.item);
        itemj <- item.toJson
      ) yield {
        Json.obj(
          "title" -> ce.title,
          "note" -> ce.note,
          "kind" -> Json.toJson(ce.kind),
          "tags" -> ce.tags,
          "item" -> itemj
        )      
      }
    }

}