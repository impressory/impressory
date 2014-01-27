package com.impressory.external

import org.htmlcleaner._
import com.wbillingsley.handy._
import Ref._

/**
 * Pulls OpenGraph and other data from a web page
 */
object MetaExtractor {
  
  // For parsing OpenGraph tags etc from  
  private val htmlCleaner = new org.htmlcleaner.HtmlCleaner() 
  
  def site(url: String):Option[String] = {
    import java.net.{ URI, URISyntaxException }
    try {
      val uri = new URI(url);
      if (uri.isAbsolute) {
        val host = uri.getHost
        if (host.startsWith("www.")) {
          Some(host.substring(4))
        } else Some(host)
      } else {
        None
      }
    } catch {
      case ex: URISyntaxException => Some("(malformed url)")
    }
  }
    
  
  def fetchAndExtract(url:String):Ref[MetaData] = {
    import play.api.libs.ws.WS
    import play.api.libs.concurrent.Execution.Implicits._
  
    (for {
      res <- WS.url(url).withHeaders("Accept" -> "text/*").get()
      title = "<title>([^<]+)</title>".r.findFirstMatchIn(res.body).map(_.group(1))
    } yield {
      
      // Just take the first 10k to avoid parsing very long responses
      val excerpt = res.ahcResponse.getResponseBodyExcerpt(10000)
      val html = htmlCleaner.clean(excerpt)
      
      val metas = html.getElementsByName("meta", true)
      
      def metaName(n:TagNode) = {
        Option(n.getAttributeByName("name")) orElse Option(n.getAttributeByName("property"))
      }
      
      def findMetaByName(nodes:Seq[TagNode], name:String) = {
        metas.find { n => 
          n.hasAttribute("name") && n.getAttributeByName("name") == name
        } flatMap { n => Option(n.getAttributeByName("content")) }
      }
      
      def findMetaProperty(nodes:Seq[TagNode], property:String) = {
        metas.find { n => 
          n.hasAttribute("property") && n.getAttributeByName("property") == property
        } flatMap { n => Option(n.getAttributeByName("content")) }
      }
      
      def findTagContent(t:TagNode, tagName:String) = {
        val el = t.getElementsByName(tagName, true).headOption
        for (e <- el) yield e.getText().toString
      }      
      
      val pairs = for {
          m <- metas
          n <- metaName(m)
          lc = n.toLowerCase()
          c <- Option(m.getAttributeByName("content"))
        } yield (lc, c)
      
      val metaMap = Map(pairs:_*)
            
      val m = MetaData(
        title = {
          metaMap.get("og:title") orElse findTagContent(html, "title")
        },
        
        canonicalUrl = {
          metaMap.get("og:url") orElse Some(url)
        },
          
        imageUrl = {
          metaMap.get("og:image")
        },
        
        siteName = {
          metaMap.get("og:site_name") orElse site(url)
        },
        
        summary = {
          metaMap.get("description") 
        },
        
        noFrame = {
          res.header("X-Frame-Options").isDefined
        }
      )
      println(m)
      m
      
    }).toRef
  
  }
}

case class MetaData(
  title:Option[String] = None,
  
  canonicalUrl:Option[String] = None,
  
  imageUrl:Option[String] = None,
  
  summary: Option[String] = None,
  
  siteName: Option[String] = None,
  
  /** Whether X-Frames-Option is set */
  noFrame: Boolean = false
)

