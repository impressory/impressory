package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._

import _root_.scala.collection.mutable

import java.net.{URISyntaxException, URI}
import Permissions._

import play.api.libs.json.JsValue


case class EntryInSequence(entry: ContentEntry, index:Option[Int])

/**
 * Point of entry for making changes to ContentEntries.
 */
object ContentModel {
  
  val defaultTopic = "page one"
  
  def update(ce:ContentEntry, jsVal: JsValue) = {
    for (title <- (jsVal \ "title").asOpt[String]) { ce.title = Some(title) }
    for (sn <- (jsVal \ "note").asOpt[String]) { ce.note = Some(sn) }
    ce
  }      
  
  /**
   * Return true if the ContentEntry matches all filters
   */
  private def applyFilters(p:ContentEntry, filters:Map[String,String]):Boolean = {
    var v = true
    for ((key, value) <- filters) {
      key match {
        case "noun" => v &&= p.nouns.contains(value)
        case "adjective" => v &&= p.adjectives.contains(value)
        case "site" => v &&= (value == p.site)
        case _ => true
      }
    }
    v
  }    
    
  /**
   * Chooses one of a RefMany of ContentEntries to prefer.
   */
  private def pick(list:RefMany[ContentEntry]) = {
    val result = list.fold[Option[ContentEntry]](None) { (optFav, entry) =>
      
      (for (fav <- optFav) yield {
        if (
          (!fav.showFirst && entry.showFirst)
        ) entry else fav
      }) orElse Some(entry)
      
    }
    result.flatten
  }
  
  /**
   * New style lookup now that we have Presentations.
   * @return
   */
  def recommendCE(course:Ref[Course], tok:Approval[User], topic:Option[String], filters:Map[String,String]):Ref[ContentEntry] = {
    (for (approved <- tok ask Read(course)) yield {
      val all = ContentEntry.byTopic(course, topic.getOrElse(defaultTopic))
      val filtered = all.withFilter(applyFilters(_, filters))
      pick(filtered)
    }).flatten 
  }  
  
  /**
   * Pairs a ContentEntry wth a containing sequence
   */
  def entryInSequence(entry:Ref[ContentEntry], seq:Ref[ContentEntry] = RefNone):Ref[EntryInSequence] = {
    
    val rr:Ref[Ref[EntryInSequence]] = for (eOpt <- optionally(entry)) yield {
      // Look at the entry first.
      eOpt match {
        case Some(e) => {
          e.item match {
            // If this is a sequence, return its title page
            case Some(cs:ContentSequence) => {
              EntryInSequence(e, None).itself
            }
            
            // Otherwise, check the sequence contains this ContentEntry
            case _ => {
              val checkedSeq = seq.withFilter { seqCE =>
                seqCE.item match {
                  case Some(cs: ContentSequence) => cs.contains(e.itself)
                  case _ => false
                }
              } orIfNone ContentSequence.containing(e.itself).first
              
              
              (for (s <- checkedSeq) yield {
                EntryInSequence(s, s.item match {
                  case Some(cs: ContentSequence) => Some(cs.indexOf(e.itself))
                  case _ => None
                })
              }) orIfNone EntryInSequence(e, None).itself
            }
          }
          
        }
        case None => {
          (for (s <- seq) yield {
            s.item match {
              case Some(cs:ContentSequence) => EntryInSequence(s, None).itself
              case _ => RefNone
            }
          }).flatten
        }
      }
    }
    rr.flatten
  }
  
}
