package com.impressory.play.model

import com.wbillingsley.handy._
import play.api.libs.json._
import Ref._
import play.api.libs.iteratee.Enumerator

object JsonConverters {
  
  trait WritesRJ[O] {
    def writeRJ(obj: O):Ref[JsValue]
  }
  
  implicit object UserWritesRJ extends WritesRJ[User] {
    def writeRJ(user: User) = user.itself.toJson
  } 
  
  implicit object ContentEntryWritesRJ extends WritesRJ[ContentEntry] {
    def writeRJ(entry: ContentEntry) = entry.itself.toJson
  }
  
  implicit object JsonWritesRJ extends WritesRJ[JsValue] {
    def writeRJ(j: JsValue) = j.itself
  }
  
  
  /** 
   * Useful for putting the commas in the right place when Enumerating JSON as a string
   */
  implicit class StringifyJson(val en: Enumerator[JsObject]) extends AnyVal {
    def stringify = {
      var sep = ""
      for (j <- en) yield {
        val s = sep + j.toString
        sep = ","
        s
      }
    }
  }  

  implicit class StringifyJsValue(val en: Enumerator[JsValue]) extends AnyVal {
    def stringify = {
      var sep = ""
      for (j <- en) yield {
        val s = sep + j.toString
        sep = ","
        s
      }
    }
  }  
  
  
  implicit class UserToJson(val u: Ref[User]) extends AnyVal {
        
    /**
     * JSON representation for other users
     */
    def toJson: Ref[JsObject] = {
      for (
        user <- u
      ) yield {
        Json.obj(
          "id" -> user.id.stringify,
          "name" -> user.name, 
          "nickname" -> user.nickname,
          "username" -> user.username,
          "avatar" -> user.avatar
        )
      }
    }
    
    /**
     * JSON representation for other users
     */
    def toJsonForSelf: Ref[JsValue] = {
      for (
        user <- u;
        j <- user.itself.toJson;
        identities <- user.identities.toRefMany.toJson
      ) yield {
        j ++ Json.obj(
          "email" -> user.email,
          "avatar" -> user.avatar,
          "passSet" -> user.pwhash.isDefined,
          "identities" -> identities.toSeq
        )
      }
    }    
  }
  
  implicit class IdentityToJson(val i: Ref[Identity]) extends AnyVal {
    def toJson: Ref[JsValue] = {
      for (identity <- i) yield {
        Json.obj(
          "id" -> identity._id.stringify,
          "service" -> identity.service,
          "avatar" -> identity.avatar
        )
      }
    }
  }
  
  implicit class ManyIdentityToJson(val i: RefMany[Identity]) extends AnyVal {
    def toJson = i.flatMap(_.itself.toJson).toRefOne
  }
  
  implicit class RegistrationToJson(val r: Ref[Registration]) extends AnyVal {
    def toJson: Ref[JsValue] = {
      for (reg <- r) yield Json.obj(
        "course" -> reg.course.getId.map(_.stringify),
        "roles" -> reg.roles.map(_.toString), 
        "created" -> reg.created,
        "updated" -> reg.updated
      )
    }
  }
  
  implicit class CourseToJson(val c: Ref[Course]) extends AnyVal {
    
    /**
     * Raw JSON for a course
     */
    def toJson = {
      for (course <- c) yield {
        Json.obj(
          "id" -> course.id.stringify,
          "title" -> course.title,
          "shortName" -> course.shortName,
          "shortDescription" -> course.shortDescription,
          "longDescription" -> course.longDescription,
          "coverImageURL" -> course.coverImageURL,
          "listed" -> course.listed,
          "edition" -> course.edition
        )
      }
    }
    
    /**
     * JSON for a Course, including registration and permission 
     * information for this User.
     */
    def toJsonForAppr(appr:Approval[User]) = {
      val rr = for (
        course <- c;
        cj <- course.itself.toJson
      ) yield {
        // Registrations. Note, can produce RefNone
        val reg = for (
            u <- appr.who;
            r <- Ref(u.registrations.find(_.course.getId == Some(course.id)));
            json <- r.itself.toJson
        ) yield json
        
        // Permissions.
        val perms = for (
           read <- optionally(appr ask Permissions.Read(course.itself));
           edit <- optionally(appr ask Permissions.EditCourse(course.itself))
        ) yield Json.obj(
          "read" -> read.isDefined,
          "edit" -> edit.isDefined
        )
        
        // Combine the JSON responses, noting that reg or perms might be RefNone
        for (r <- optionally(reg); p <- optionally(perms)) yield cj ++ Json.obj(
          "registration" -> r,
          "permissions" -> p
        )
      }
      rr.flatten
    }
  }
  
  /**
   * Converts ContentItems to JSON format. This uses a partial function so that
   * new ContentItems can register themselves without needing to edit and recompile
   * this class.
   */
  implicit object ContentItemToJson {
    
    /**
     * The partial function (which includes all registered partial functions) for
     * converting to JSON
     */
    var pf:PartialFunction[ContentItem, Ref[JsValue]] = {
      case cs:ContentSequence => cs.itself.toJson
      case gs:GoogleSlides => OtherExternalsModel.GoogleSlidesToJson.writes(gs).itself
      case y:YouTubeVideo => OtherExternalsModel.YouTubeVideoToJson.writes(y).itself
      case wp:WebPage => WebPageModel.toJson(wp).itself
      case mp:MarkdownPage => MarkdownPageModel.toJson(mp).itself
      case p:MultipleChoicePoll => MCPollModel.MCPollToJson.writes(p).itself
      case _ => JsNull.itself 
    } 
    
    /**
     * Some content items can add additional information for a given user/Approval
     */
    var pfFor:PartialFunction[(ContentItem, Approval[User]), Ref[JsValue]] = {
      case (cs:ContentSequence, appr) => cs.itself.toJsonForAppr(appr)
      case (item, appr) => pf(item) 
    }
    
    def writes(ci:ContentItem) = pf(ci)
    
    def writesFor(ci:ContentItem, appr:Approval[User]) = pfFor(ci, appr)
    
  }
  
  implicit class ContentItemToJson(val ci: ContentItem) extends AnyVal {
    def toJson = ContentItemToJson.writes(ci)
    
    def toJsonFor(appr:Approval[User]) = ContentItemToJson.writesFor(ci, appr)
  }
  
  
  implicit class ContentEntryToJson(val entry: Ref[ContentEntry]) extends AnyVal {

    /**
     * Basic core JSON other methods add to
     */
    def toJsonCore = {
      for (
        ce <- entry
      ) yield Json.obj(
        "id" -> ce.id.stringify,
        "course" -> ce.course,
        "protect" -> ce.protect,
        "showFirst" -> ce.showFirst,
        "inTrash" -> ce.inTrash,
        "voting" -> ce.voting.toJson,
        "commentCount" -> ce.commentCount,
        "title" -> ce.title,
        "note" -> ce.note,
        "kind" -> Json.toJson(ce.kind),
        "adjectives" -> ce.adjectives,
        "nouns" -> ce.nouns,
        "topics" -> ce.topics,
        "site" -> ce.site,
        "updated" -> ce.updated,
        "created" -> ce.created,
        "addedBy" -> ce.addedBy)
    }    
    
    /**
     * JSON for a course, without permissions etc
     */
    def toJson:Ref[JsObject] = {
      for (
        ce <- entry;
        cj <- ce.itself.toJsonCore;
        item <- Ref(ce.item);
        itemj <- item.toJson
      ) yield {
        cj ++ Json.obj("item" -> itemj)
      }
    }
    
    
    /**
     * JSON for a Course, including permissions 
     * information for this User.
     */
    def toJsonForAppr(appr:Approval[User]):Ref[JsObject] = {
      val rr = for (
        ce <- entry;
        item <- Ref(ce.item);
        itemj <- item.toJsonFor(appr)
      ) yield {
        
        // Permissions.
        val perms = for (
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
        
        // Combine the JSON responses, noting that reg or perms might be RefNone
        for (ej <- ce.itself.toJson; p <- optionally(perms)) yield ej ++ Json.obj(
          "permissions" -> p,
          "item" -> itemj,
          "voting" -> ce.voting.toJsonFor(appr),
          "comments" -> ce.comments.map(_.toJsonFor(appr))
        )
      }
      rr.flatten
    }    
    
  }
  
  implicit class EntryInSequenceToJson(val eInSeq: Ref[EntryInSequence]) extends AnyVal {

    def toJson = {
      for (
        eis <- eInSeq;
        entry <- eis.entry.itself.toJson
      ) yield Json.obj(
        "entry" -> entry,
        "seqIndex" -> eis.index.orElse(Some(-1)))
    }
    
    def toJsonForAppr(appr:Approval[User]) = {
      for (
        eis <- eInSeq;
        entry <- eis.entry.itself.toJsonForAppr(appr)
      ) yield Json.obj(
        "entry" -> entry,
        "seqIndex" -> eis.index.orElse(Some(-1)))
    }    
  }

  implicit class ContentSequenceToJson(val rcs: Ref[ContentSequence]) extends AnyVal {

    def toJson = {
      for (
        cs <- rcs;
        
        // This ensures we don't go into an infinite loop if a sequence has somehow included its own item
        filteredEntries = cs.entries.withFilter(_.item match {
          case Some(cs:ContentSequence) => false;
          case _ => true
        });
        
        entries <- filteredEntries.flatMap(_.itself.toJson).toRefOne
      ) yield {
        println(s"ENTRYIDS ${cs._entries}")
        println(s"ENTRIES $entries")
        Json.obj(
        "entries" -> entries.toSeq)
      }
    }
    
    def toJsonForAppr(appr:Approval[User]) = {
      for (
        cs <- rcs;
        entries <- cs.entries.withFilter(_.kind != ContentSequence.itemType).flatMap(_.itself.toJsonForAppr(appr)).toRefOne
      ) yield Json.obj(
        "entries" -> entries.toSeq)      
    }
  }
  
  implicit class CourseInviteToJson(val rci:Ref[CourseInvite]) extends AnyVal {
    def toJson = for (ci <- rci) yield Json.obj(
      "code" -> ci.code,
      "used" -> ci._usedBy.length,
      "limitedNumber" -> ci.limitedNumber,
      "remaining" -> ci.remaining,
      "roles" -> ci.roles.map(_.toString())
    )
  }

  implicit object WritesRecordedChatEvent extends Writes[RecordedChatEvent] {
    val ChatCommentToJson = Json.writes[ChatComment]
    
    def writes(rce:RecordedChatEvent) = rce match {
      case cc:ChatComment => Json.obj("kind" -> "push", "type" -> "chat") ++ ChatCommentToJson.writes(cc)
      case _ => Json.obj("error" -> "unrecognised event")
    }
  }
  
  implicit class UpDownVotingToJson(val udv:UpDownVoting) extends AnyVal {
    def toJson = Json.obj("score" -> udv.score)
    
    def toJsonFor(a: Approval[User]) = {
      a.who.getId match {
        case Some(id) => Json.obj(
          "score" -> udv.score,
          "voted" -> (udv._up.contains(id) || udv._down.contains(id))
        )
        case None => toJson
      }
    }
  }
  
  implicit class QnAAnswerToJson(val ans:QnAAnswer) extends AnyVal {
    def toJsonFor(a: Approval[User]) = Json.obj(
      "id" -> ans._id.stringify,
      "text" -> ans.text,
      "addedBy" -> ans.addedBy,
      "voting" -> ans.voting.toJsonFor(a),
      "created" -> ans.created,
      "updated" -> ans.updated,
      "comments" -> ans.comments.map(_.toJsonFor(a))
    )
  }
  
  implicit class EmbeddedCommentToJson(val ec:EmbeddedComment) extends AnyVal {
    def toJsonFor(a: Approval[User]) = Json.obj(
      "id" -> ec._id.stringify,
      "text" -> ec.text,
      "addedBy" -> ec.addedBy,
      "voting" -> ec.voting.toJsonFor(a),
      "created" -> ec.created
    )
    
  }
  
  implicit class QnAQuestionToJson(val rq:Ref[QnAQuestion]) extends AnyVal {
    def toJsonFor(a: Approval[User]) = for (
      q <- rq
    ) yield {
      Json.obj(
        "id" -> q.id,
        "title" -> q.title,
        "text" -> q.text,
        "views" -> q.views,
        "voting" -> q.voting.toJsonFor(a),
        "answerCount" -> q.answerCount,
        "answers" -> q.answers.map(_.toJsonFor(a)),
        "commentCount" -> q.commentCount,
        "comments" -> q.comments.map(_.toJsonFor(a)),
        "addedBy" -> q.addedBy,
        "created" -> q.created,
        "updated" -> q.updated
      )
    }
    
  }

  
}