package com.impressory.play.model

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.wbillingsley.handy._
import Ref._
import org.specs2.matcher.EventuallyMatchers._

class WhatIsItTest extends Specification {
      
  "ContentTypeListing" should {
    
    "recognise YouTube" in {      
      val trialists = Seq(
        // straight url
        "https://www.youtube.com/watch?v=CvJp1X3qiog" -> "CvJp1X3qiog",
        
        // embed code
        """
<iframe width="560" height="315" src="//www.youtube.com/embed/z2HJ7-Z1X4o?rel=0" frameborder="0" allowfullscreen></iframe>        
        """ -> "z2HJ7-Z1X4o"
      )
      
      for ((text, videoId) <- trialists) {        
        val refItem = for (c <- ContentTypeListing.whatIsIt(text); i <- c.item) yield i
        refItem.fetch must equalTo(YouTubeVideo(Some(text), Some(videoId)).itself)
      }
    }
    

    "recognise Google Slides" in {
     
      val trialists = Seq(
        // straight url
        "https://docs.google.com/presentation/d/1DnHR-_KwAApUnENwlf1qTAygGQvFv2Okk4T3aQoeP4s/pub?start=false&loop=false&delayms=3000" -> "1DnHR-_KwAApUnENwlf1qTAygGQvFv2Okk4T3aQoeP4s",
        
        // embed code
        """
<iframe src="https://docs.google.com/presentation/d/1DnHR-_KwAApUnENwlf1qTAygGQvFv2Okk4T3aQoeP4s/embed?start=false&loop=false&delayms=3000" frameborder="0" width="960" height="749" allowfullscreen="true" mozallowfullscreen="true" webkitallowfullscreen="true"></iframe>        
        """ -> "1DnHR-_KwAApUnENwlf1qTAygGQvFv2Okk4T3aQoeP4s"
      )
      
      for ((text, id) <- trialists) yield {
        val refItem = for (c <- ContentTypeListing.whatIsIt(text); i <- c.item) yield i
        refItem.fetch must equalTo(GoogleSlides(Some(text), Some(id)).itself)
      }      
    }
  }

}