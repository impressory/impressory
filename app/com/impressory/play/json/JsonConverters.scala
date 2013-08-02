package com.impressory.play.json

import com.impressory.play.model._

import com.wbillingsley.handy._
import play.api.libs.json._
import com.wbillingsley.handy.Ref._
import play.api.libs.iteratee.Enumerator
import com.impressory.api.CanSendToClient
import com.impressory.play.model.EntryInSequence
import com.impressory.play.model.MCPollModel
import com.impressory.play.model.MarkdownPageModel
import com.impressory.play.model.OtherExternalsModel
import com.impressory.play.model.WebPageModel
import com.wbillingsley.handy.Approval.wrapApproval
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json.toJsFieldJsValueWrapper

object JsonConverters {
    
  implicit class CanSendToClientToJson(val obj:CanSendToClient) extends AnyVal {
    def toJsonFor(appr:Approval[User]):Ref[JsValue] = {
      obj match {
        case udv:UpDownVoting => UpDownVotingToJson.toJsonFor(udv, appr).itself
        case ec:EmbeddedComment => EmbeddedCommentToJson.toJsonFor(ec, appr).itself
        
        case ce:ContentEntry => ContentEntryToJson.toJsonFor(ce, appr)
        case q:QnAQuestion => QnAQuestionToJson.toJsonFor(q, appr).itself
        case q:QnAAnswer => QnAAnswerToJson.toJsonFor(q, appr).itself
        
        case u:User => UserToJson.toJsonFor(u, appr)        
        case r:Registration => RegistrationToJson.toJson(r).itself
        
        case c:Course => CourseToJson.toJsonFor(c, appr)
        case ci:CourseInvite => CourseInviteToJson.toJson(ci).itself
        
        case eis:EntryInSequence => EntryInSequenceToJson.toJsonFor(eis, appr)
        
        case cs:ContentSequence => ContentSequenceToJson.toJsonFor(cs, appr)
        case gs:GoogleSlides => OtherExternalsModel.GoogleSlidesToJson.writes(gs).itself
        case y:YouTubeVideo => OtherExternalsModel.YouTubeVideoToJson.writes(y).itself
        case wp:WebPage => WebPageModel.toJson(wp).itself
        case mp:MarkdownPage => MarkdownPageModel.toJson(mp).itself
        case p:MultipleChoicePoll => MCPollModel.MCPollToJson.writes(p).itself
        
        case _ => RefFailed(new IllegalArgumentException(s"The ${obj.getClass.getName} could not be written out in JSON format"))
      }
    }  
    
    def toJson:Ref[JsValue] = {
      obj match {
        case udv:UpDownVoting => UpDownVotingToJson.toJson(udv).itself
        case ce:ContentEntry => ContentEntryToJson.toJson(ce)
        case eis:EntryInSequence => EntryInSequenceToJson.toJson(eis)        
        case u:User => UserToJson.toJson(u).itself
        case c:Course => CourseToJson.toJson(c).itself
        
        case _ => toJsonFor(Approval(RefNone))
      }
    }      
  }
  
  implicit class RefCanSendToClientToJson(val ref:Ref[CanSendToClient]) extends AnyVal {
    def toJsonFor(appr:Approval[User]) = {
      for (obj <- ref; j <- obj.toJsonFor(appr)) yield j
    }    

    def toJson = {
      for (obj <- ref; j <- obj.toJson) yield j
    }      
  }
  
  implicit class RefManyCanSendToClientToJson(val ref:RefMany[CanSendToClient]) extends AnyVal {
    def toJsonFor(appr:Approval[User]) = {
      val rmj = for (obj <- ref; j <- obj.toJsonFor(appr)) yield j
      rmj.toRefOne.map(t => Json.toJson(t.toSeq))
    }    
    
    def toJson = {
      val rmj = for (obj <- ref; j <- obj.toJson) yield j
      rmj.toRefOne.map(t => Json.toJson(t.toSeq))
    }    
  }
  
  /**
   * Typeclass used in RefConversions to convert a returned object to JSON for the REST interface
   */
  trait WritesRJ[O] {
    def writeRJ(obj: O):Ref[JsValue]
  }
  
  /**
   * Typeclass to mark that our CanSendToClient classes can be converted to JSON
   */
  implicit def canSendToClientWritesRJ[T <: CanSendToClient] = new WritesRJ[T] {
    def writeRJ(obj: T) = obj.toJson
  } 
  
  /**
   * JSON itself can be converted to Ref[JSON]
   */
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

  /** 
   * Useful for putting the commas in the right place when Enumerating JSON as a string.
   * We have both this and StringifyJson because Enumerator[T] is invariant on T
   */
  implicit class StringifyJsValue[J <: JsValue](val en: Enumerator[J]) extends AnyVal {
    def stringify = {
      var sep = ""
      for (j <- en) yield {
        val s = sep + j.toString
        sep = ","
        s
      }
    }
  }  
  

  implicit object WritesRecordedChatEvent extends Writes[RecordedChatEvent] {
    val ChatCommentToJson = Json.writes[ChatComment]
    
    def writes(rce:RecordedChatEvent) = rce match {
      case cc:ChatComment => Json.obj("kind" -> "push", "type" -> "chat") ++ ChatCommentToJson.writes(cc)
      case _ => Json.obj("error" -> "unrecognised event")
    }
  }
  
}