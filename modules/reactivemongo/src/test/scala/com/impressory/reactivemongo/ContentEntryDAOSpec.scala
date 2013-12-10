package com.impressory.reactivemongo

import org.specs2.mutable._
import com.wbillingsley.handy._
import Ref._
import scala.concurrent.ExecutionContext.Implicits.global
import com.impressory.api._
import org.specs2.specification.BeforeExample
import org.specs2.specification.Step

class ContentEntryDAOSpec extends DatabaseSpec {
  
  sequential
      
  "ContentEntryDAO" should {
    
    "save a new entry" in {  
      val c = new LazyId(classOf[Course], CourseDAO.allocateId)
      val u = new LazyId(classOf[User], UserDAO.allocateId)
      
      val e = ContentEntryDAO.unsaved.copy(
        course=c, addedBy=u,
        title=Some("Algernon Moncrieff"),
        item=Some(MarkdownPage(text="Hello"))
      )
      
      val returnedText = for {
          saved <- ContentEntryDAO.saveNew(e);      
          fetched <- ContentEntryDAO.byId(e.id); 
          item <- fetched.item.toRef
      } yield item.asInstanceOf[MarkdownPage].text      
      returnedText.toFuture must be_==(Some("Hello")).await      
    }
    
  }

}