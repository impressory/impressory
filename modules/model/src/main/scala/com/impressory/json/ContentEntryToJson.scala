package com.impressory.json

import com.impressory.api._
import com.impressory.plugins.LookUps._
import com.impressory.security.Permissions
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.RefMany._
import play.api.libs.json._
import com.wbillingsley.handyplay.JsonConverter


object ContentEntryToJson extends JsonConverter[ContentEntry, User] {
  
  implicit val settingsFormat = Json.format[CESettings]

  implicit val messageFormat = Json.format[CEMessage]

  implicit val tagsFormat = Json.format[CETags]
  
    /**
     * Basic core JSON other methods add to
     */
    def toJson(ce:ContentEntry) = {
      for {
        voting <- UpDownVotingToJson.toJson(ce.voting)
        comments <- CommentsToJson.toJson(ce.comments)
        item <- optionally(for {
          i <- ce.item.toRef
          j <- ContentItemToJson.toJson(ce, i)
        } yield j)
      } yield Json.obj(
        "id" -> ce.id,
        "course" -> ce.course,
        "addedBy" -> ce.addedBy,
        "settings" -> ce.settings,
        "message" -> ce.message,
        "kind" -> Json.toJson(ce.kind),
        "item" -> item,
        "voting" -> voting,
        "comments" -> comments,
        "tags" -> ce.tags,
        "updated" -> ce.updated,
        "created" -> ce.created
      )
    }    

    
    /**
     * Permissions on the content entry
     */
    def permissions(ce:ContentEntry, appr:Approval[User]) = for (
       add <- optionally(appr ask Permissions.addContent(ce.course));
       read <- optionally(appr ask Permissions.readEntry(ce.itself));
       edit <- optionally(appr ask Permissions.editContent(ce.itself));
       vote <- optionally(appr ask Permissions.voteOnEntry(ce.itself));
       comment <- optionally(appr ask Permissions.commentOnEntry(ce.itself))
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
      for {
        voting <- UpDownVotingToJson.toJsonFor(ce.voting, appr)
        comments <- CommentsToJson.toJsonFor(ce.comments, appr)
        item <- optionally(for {
          i <- ce.item.toRef
          j <- ContentItemToJson.toJson(ce, i)
        } yield j)
        p <- optionally(permissions(ce, appr))
      } yield Json.obj(
        "id" -> ce.id,
        "course" -> ce.course,
        "addedBy" -> ce.addedBy,
        "settings" -> ce.settings,
        "message" -> ce.message,
        "kind" -> Json.toJson(ce.kind),
        "item" -> item,
        "voting" -> voting,
        "comments" -> comments,
        "tags" -> ce.tags,
        "updated" -> ce.updated,
        "created" -> ce.created,
        "permissions" -> p
      )
    }  
    
    /**
     * Cut-down JSON, that would be acceptable being received from the client.
     * Used by ContentController.whatIsIt
     */
    def toJsonForInput(ce:ContentEntry) = {      
      for (
        item <- Ref(ce.item);
        itemj <- ContentItemToJson.toJsonFor(ce, item, Approval(RefNone))
      ) yield {
        Json.obj(
          "message" -> ce.message,
          "kind" -> Json.toJson(ce.kind),
          "tags" -> ce.tags,
          "item" -> itemj
        )      
      }
    }

  def update(ce:ContentEntry, jsVal: JsValue) = {
    ce.copy(
      message = (jsVal \ "message").asOpt[CEMessage] getOrElse ce.message,
      tags = (jsVal \ "tags").asOpt[CETags] getOrElse ce.tags,
      settings = (jsVal \ "settings").asOpt[CESettings] getOrElse ce.settings
    )
  }
}