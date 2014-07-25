package com.impressory.external

import org.htmlcleaner._
import com.wbillingsley.handy._
import Ref._

import play.api.Play.current
import play.api.libs.iteratee.{Iteratee, Enumerator, Enumeratee}
import play.api.libs.ws.WSResponseHeaders
import play.api.libs.ws.ning.NingWSResponse
import play.api.mvc.Codec


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


  def extractFromHtml(url:String, headers:WSResponseHeaders, excerpt:String):MetaData = {
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


    try {
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
          headers.headers.isDefinedAt("X-Frame-Options")
        }
      )
      m
    } catch {
      case x:Throwable => {
        x.printStackTrace()
        throw x
      }
    }
  }

  def charSet(contentType:Option[String]):String = contentType match {
    case Some(ct) => {
      val arr = ct.split("charset=")
      if (arr.length == 2) {
        arr(1)
      } else "utf-8"
    }
    case _ => "utf-8"
  }

  def extractFromTextlikeStream(url:String, headers:WSResponseHeaders, stream:Enumerator[Array[Byte]]):Ref[MetaData] = {

    import play.api.libs.concurrent.Execution.Implicits._

    val contentType = headers.headers("Content-Type").headOption
    val textCharSet = charSet(contentType)

    var taken = 0
    val limited = stream through Enumeratee.takeWhile { arr =>
      taken += arr.length
      taken < 10000
    }

    val codec:Codec = Codec.javaSupported(textCharSet)
    val stringed = limited through Enumeratee.map { arr =>
      codec.decode(arr)
    }

    val futRes = stringed.run(Iteratee.consume[String].apply)
    for {
      excerpt <- futRes
    } yield {
      extractFromHtml(url, headers, excerpt)
    }
  }

  
  def fetchAndExtract(url:String):Ref[MetaData] = {
    import play.api.libs.ws.WS
    import play.api.libs.concurrent.Execution.Implicits._

    var taken = 0
    for {
      (res, enum) <- WS.url(url).withHeaders("Accept" -> "text/*").getStream().toRef
      meta <- extractFromTextlikeStream(url, res, enum)
    } yield meta
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

