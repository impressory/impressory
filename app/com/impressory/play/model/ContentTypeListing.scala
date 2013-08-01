package com.impressory.play.model

import com.wbillingsley.handy._
import play.api.libs.json.JsValue


/**
 * Given a URL or an embed code, works out what kind of content item it is.
 */
object ContentTypeListing {
  
  type CodeCheck = (String) => Ref[ContentItem]
  
  var checkers:Seq[CodeCheck] = Seq(OtherExternalsModel.youTubeMatcher, OtherExternalsModel.googleSlidesMatcher, WebPageModel.urlMatcher)
  
  def addChecker(c:CodeCheck) { checkers :+= c }
  
  def whatIsIt(url:String) = {
    checkers.foldLeft[Ref[ContentItem]](RefNone)(_ orIfNone _(url))
  }

}