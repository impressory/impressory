package com.impressory.json

import com.impressory.api._
import com.impressory.security.Permissions
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.RefMany._
import play.api.libs.json._
import com.wbillingsley.handy.appbase.JsonConverter


object ContentEntryToJson extends JsonConverter[ContentEntry, User] {
  
  implicit val settingsFormat = Json.format[CESettings]
  
  implicit val tagsFormat = Json.format[CETags]
  
    /**
     * Basic core JSON other methods add to
     */
    def toJsonCore(ce:ContentEntry) = {
      for {
        voting <- UpDownVotingToJson.toJson(ce.voting)
      } yield Json.obj(
        "id" -> ce.id,
        "course" -> ce.course,
        "settings" -> ce.settings,
        "voting" -> voting,
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
            
      for {
        j <- toJsonCore(ce)
        item <- ce.item.toRef
        itemj <- ContentItemToJson.toJson(item)
      } yield {
        j ++ Json.obj("item" -> itemj)
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
        itemj <- ContentItemToJson.toJsonFor(item, appr);
        voting <- UpDownVotingToJson.toJsonFor(ce.voting, appr);
        comments <- (
          for {
            c <- ce.comments.toRefMany
            j <- EmbeddedCommentToJson.toJsonFor(c, appr) 
          } yield j
        ).toRefOne
      ) yield ej ++ Json.obj(
          "permissions" -> p,
          "item" -> itemj,
          "voting" -> voting,
          "comments" -> comments.toSeq
      )
    }  
    
    /**
     * Cut-down JSON, that would be acceptable being received from the client.
     * Used by ContentController.whatIsIt
     */
    def toJsonForInput(ce:ContentEntry) = {      
      for (
        item <- Ref(ce.item);
        itemj <- ContentItemToJson.toJsonFor(item, Approval(RefNone))
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

  def update(ce:ContentEntry, jsVal: JsValue) = {
    for (p <- (jsVal \ "setPublished").asOpt[Boolean]) { ce.setPublished(p) }
    ce.copy(
      title = (jsVal \ "title").asOpt[String] orElse ce.title,
      note = (jsVal \ "note").asOpt[String] orElse ce.note,
      tags = (jsVal \ "tags").asOpt[CETags] getOrElse ce.tags,
      settings = (jsVal \ "settings").asOpt[CESettings] getOrElse ce.settings
    )
  }
}