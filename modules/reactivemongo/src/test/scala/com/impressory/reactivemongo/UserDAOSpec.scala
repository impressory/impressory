package com.impressory.reactivemongo

import org.specs2.mutable._
import com.wbillingsley.handy._
import Ref._
import scala.concurrent.ExecutionContext.Implicits.global
import com.impressory.api._
import org.specs2.specification.BeforeExample
import org.specs2.specification.Step

class UserDAOSpec extends DatabaseSpec {
  
  sequential
      
  "UserDAO" should {
    
    "save a new user" in {      
      val u = UserDAO.unsaved.copy(name=Some("Algernon Moncrieff"))      
      
      val returnedName = for (
          saved <- UserDAO.saveNew(u);      
          fetched <- UserDAO.byId(u.id); 
          name <- fetched.name
      ) yield name      
      returnedName.toFuture must be_==(Some("Algernon Moncrieff")).await      
    }
    
    "push identities correctly" in {      
      val u = UserDAO.unsaved.copy(name=Some("Bertie Wooster"))      
      
      val returnedName = for (
        saved <- UserDAO.saveNew(u);      
        pushed <- UserDAO.pushIdentity(saved.itself, Identity(service="spoodle", value="spong"));
        fetched <- UserDAO.byIdentity(service="spoodle", id="spong"); name <- fetched.name
      ) yield name      
      returnedName.toFuture must be_==(Some("Bertie Wooster")).await      
    }
    
    "push sessions correctly" in {      
      val u = UserDAO.unsaved.copy(name=Some("Cecily Cardew"))      
      
      val returnedName = for (
        saved <- UserDAO.saveNew(u);      
        pushed <- UserDAO.pushSession(saved.itself, ActiveSession(key="mysession", ip="local"));
        fetched <- UserDAO.bySessionKey("mysession"); name <- fetched.name
      ) yield name      
      
      returnedName.toFuture must be_==(Some("Cecily Cardew")).await      
    }
    
    "push registrations correctly" in {      
      val u = UserDAO.unsaved.copy(name=Some("Dahlia Travers"))
      val fakeCourseRef = new LazyId(classOf[Course], CourseDAO.allocateId)
      val reg = new Registration(course=fakeCourseRef, roles=Set(CourseRole.Reader, CourseRole.Chatter))
      
      val returnedReg = for {
        saved <- UserDAO.saveNew(u)
        pushed <- UserDAO.pushRegistration(saved.itself, reg)
        fetched <- UserDAO.byId(saved.id)
        r <- fetched.registrations.headOption.toRef
      } yield (r.course.getId, r.roles)      
      
      returnedReg.toFuture must be_==(Some((reg.course.getId, reg.roles))).await      
    }
    
    "hash passwords consistently for a user's salt" in {
      val pwd = "Jack Worthing"
      val unsaved = UserDAO.unsaved
      val u = unsaved.copy(name=Some("Ernest Moncrieff"), pwlogin=unsaved.pwlogin.copy(pwhash=unsaved.pwlogin.hash(pwd)))

      val returnedHash = for (
          saved <- UserDAO.saveNew(u);
          fetched <- UserDAO.byId(u.id) 
      ) yield fetched.pwlogin.hash(pwd)      
      returnedHash.toFuture must be_==(Some(u.pwlogin.pwhash)).await      
    }
    
    "give new users the Reader and Author site roles" in {
      val unsaved = UserDAO.unsaved
      val u = unsaved.copy(name=Some("Kimball O'Hara"))

      val returnedRoles = for (
          saved <- UserDAO.saveNew(u);
          fetched <- UserDAO.byId(u.id) 
      ) yield fetched.siteRoles    
      returnedRoles.toFuture must be_==(Some(Set(SiteRole.Author, SiteRole.Reader))).await      
      
    }
    
  }

}