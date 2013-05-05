package com.impressory.play.model


import com.wbillingsley.handy._
import Ref._
import Permissions._
import play.api.libs.json._


/**
 *  
 */
object WebPageModel {

  def site(url: String) = {
    import java.net.{ URI, URISyntaxException }

    try {
      val uri = new URI(url);
      if (uri.isAbsolute) {
        val host = uri.getHost
        if (host.startsWith("www.")) {
          host.substring(4)
        } else host
      } else {
        "local"
      }
    } catch {
      case ex: URISyntaxException =>
        "(malformed url)"
    }
  }
  
  /**
   * Creates but does not save a ContentSequence, wrapped in a ContentEntry
   */
  def create(course:Ref[Course], approval:Approval[User], ce:ContentEntry, data:JsValue) = {
    val url = (data \ "webpage" \ "url").asOpt[String]
    
    ce.site = url map { s => site(s) } getOrElse("(none)")
    
    WebPage.unsaved(course, ce.itself, url)
  }  

}