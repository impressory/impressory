package com.impressory.plugins

import com.wbillingsley.handy._
import com.impressory.api._
import com.impressory.json._

import play.api.libs.json._


/**
 * Given a URL or an embed code, works out what kind of content item it is.
 */
object ContentTypeListing {
  
  type CodeCheck = (ContentEntry, String) => Ref[ContentEntry]
  
  var checkers:Seq[CodeCheck] = Seq.empty
  
  def addChecker(c:CodeCheck) { checkers :+= c }
  
  def whatIsIt(blank:ContentEntry, url:String) = {
    checkers.foldLeft[Ref[ContentEntry]](RefNone)(_ orIfNone _(blank, url))
  }

  def registerHandler[T <: ContentItem](jh:ContentItemJsonHandler[T]) = {

    checkers = checkers :+ { (blank, s) => jh.urlChecker(blank, s) }
    
    ContentItemToJson.toJsonPF = ContentItemToJson.toJsonPF orElse {  
      case (i, appr) if  (jh.clazz isAssignableFrom i.getClass) => jh.toJsonFor(i.asInstanceOf[T], appr) 
    }
    
    ContentItemToJson.createFromJsonPF = ContentItemToJson.createFromJsonPF orElse {  
      case (kind, blank, json) if (kind == jh.kind) => jh.createFromJson(blank, json) 
    }

    ContentItemToJson.updateFromJsonPF = ContentItemToJson.updateFromJsonPF orElse {  
      case (before, json) if (before.kind == jh.kind) => jh.updateFromJson(before, json) 
    }
  }
  
}


trait ContentItemJsonHandler[T <: ContentItem] {
  
  val kind: String
  
  val clazz: Class[T]
  
  def urlChecker(blank:ContentEntry, url:String):Ref[ContentEntry]
  
  def createFromJson(blank:ContentEntry, json:JsValue):Ref[ContentEntry]
  
  def updateFromJson(before:ContentEntry, json:JsValue):Ref[ContentEntry]
  
  def toJsonFor(item:T, appr:Approval[User]): Ref[JsObject]
}
