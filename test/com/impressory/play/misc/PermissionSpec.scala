package com.impressory.play.json

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.wbillingsley.handy._
import Ref._

import com.impressory.api._
import com.impressory.security._

import com.impressory.play.model._

import play.api.libs.json._

class PermissionSpec extends Specification {
      
  "Permissions" should {
    
    "cache approvals" in {      
      val u = new User(id="1")
      val c = new Course(id="1")
      
      val approval = Approval(u.itself)      
      val approved = approval ask Permissions.Read(c.itself)      
      approval.permissions.contains(Permissions.Read(c.itself)) must_== true 
    }    
    
    "find approvals in the cache" in {      
      val u = new User(id="1")
      val c = new Course(id="1")
      
      val appr = Approval(u.itself)
      
      val perm = Permissions.Read(c.itself)
      appr.permissions.add(perm)
      
      appr.ask(perm) must_== Approved("Already approved").itself 
    }
    
    "find approvals with the same ID in the cache" in {      
      val u = new User(id="1")
      val c = new Course(id="1")
      
      val appr = Approval(u.itself)
      
      val perm = Permissions.Read(c.itself)
      appr.permissions.add(perm)
      
      appr.ask(Permissions.Read(refCourse(c.id))) must_== Approved("Already approved").itself
    }    
    
    "cache delegated permissions" in {      
      val u = new User(id="1")
      val c = new Course(id="1")
      val e = new ContentEntry(id="1", course=c.itself, addedBy=u.itself)
      
      val approval = Approval(u.itself)      
      val approved = approval ask Permissions.ReadEntry(e.itself)
      approval.permissions.contains(Permissions.Read(c.itself)) must_== true
    }       
    
  }

}