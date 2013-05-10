package com.impressory.play.model

import com.wbillingsley.handy._
import play.api.libs.json._
import Ref._
import play.api.libs.iteratee.Enumerator

object JsonConverters {
  
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
  
  implicit class UserToJson(val u: Ref[User]) extends AnyVal {
        
    /**
     * JSON representation for other users
     */
    def toJson: Ref[JsObject] = {
      for (
        user <- u;
        identities <- user.identities.toRefMany.toJson
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
        identities <- user.identities.toRefMany.toJson
      ) yield {
        Json.obj(
          "id" -> user.id.stringify,
          "name" -> user.name, 
          "nickname" -> user.nickname,
          "email" -> user.email,
          "username" -> user.username,
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
        "course" -> reg._course.stringify,
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
            r <- Ref(u.registrations.find(_._course == course.id));
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
  
  
  implicit class ContentEntryToJson(val entry: Ref[ContentEntry]) extends AnyVal {

    /**
     * Basic core JSON other methods add to
     */
    def toJsonCore = {
      for (
        ce <- entry
      ) yield Json.obj(
        "id" -> ce.id.stringify,
        "course" -> ce._course.stringify,
        "protect" -> ce.protect,
        "showFirst" -> ce.showFirst,
        "inTrash" -> ce.inTrash,
//        "score" -> ce.score,
//        "commentCount" -> ce.commentCount,
        "title" -> ce.title,
        "note" -> ce.note,
        "kind" -> Json.toJson(ce.kind),
        "adjectives" -> ce.adjectives,
        "nouns" -> ce.nouns,
        "topics" -> ce.topics,
        "site" -> ce.site,
        "updated" -> ce.updated,
        "created" -> ce.created,
        "addedBy" -> ce._addedBy.stringify)
    }    
    
    /**
     * JSON for a course, without permissions etc
     */
    def toJson:Ref[JsObject] = {
      for (
        ce <- entry;
        cj <- ce.itself.toJsonCore;
        item <- optionally {
          ce.item match {
            case Some(wp:WebPage) => wp.toJson.itself
            case Some(cs:ContentSequence) => cs.itself.toJson
            case Some(gs:GoogleSlides) => GoogleSlidesToJson.writes(gs).itself
            case Some(y:YouTubeVideo) => YouTubeVideoToJson.writes(y).itself
            case _ => RefNone
          }
        }
      ) yield {
        cj ++ Json.obj("item" -> item)
      }
    }
    
    
    /**
     * JSON for a Course, including permissions 
     * information for this User.
     */
    def toJsonForAppr(appr:Approval[User]):Ref[JsObject] = {
      val rr = for (
        ce <- entry;
        item <- optionally {
          ce.item match {
            case Some(wp:WebPage) => wp.toJson.itself
            case Some(cs:ContentSequence) => cs.itself.toJsonForAppr(appr)
            case _ => RefNone
          }
        }        
      ) yield {
        
        // Permissions.
        val perms = for (
           read <- optionally(appr ask Permissions.ReadEntry(ce.itself));
           edit <- optionally(appr ask Permissions.EditContent(ce.itself))
        ) yield Json.obj(
          "read" -> read.isDefined,
          "edit" -> edit.isDefined
        )
        
        // Combine the JSON responses, noting that reg or perms might be RefNone
        for (ej <- ce.itself.toJson; p <- optionally(perms)) yield ej ++ Json.obj(
          "permissions" -> p,
          "item" -> item
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
  
  implicit class WebPageToJson(val wp: WebPage) extends AnyVal {
    def toJson = Json.obj(
      "url" -> wp.url,
      "noFrame" -> wp.noFrame
    )
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
        entries <- cs.entries.withFilter(_._id != cs._id).flatMap(_.itself.toJsonForAppr(appr)).toRefOne
      ) yield Json.obj(
        "entries" -> entries.toSeq)      
    }
  }

  
  implicit val GoogleSlidesToJson = Json.format[GoogleSlides]
  implicit val YouTubeVideoToJson = Json.format[YouTubeVideo]
  
}