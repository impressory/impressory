package com.impressory.play.json

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.wbillingsley.handy._
import Ref._
import com.impressory.play.model._
import JsonConverters._
import play.api.libs.json.Json
import play.api.libs.json.JsObject

class PermissionSpec extends Specification {
      
  "Permissions" should {
    
    "cache approvals" in {      
      val u = User.unsaved()
      val c = new Course()
      
      val approval = Approval(u.itself)      
      val approved = approval ask Permissions.Read(c.itself)      
      approval.permissions.contains(Permissions.Read(c.itself)) must_== true 
    }    
    
    "find approvals in the cache" in {      
      val u = User.unsaved()
      val c = new Course()
      
      val appr = Approval(u.itself)
      
      val perm = Permissions.Read(c.itself)
      appr.permissions.add(perm)
      
      appr.ask(perm) must_== Approved("Already approved").itself 
    }
    
    "find approvals with the same ID in the cache" in {      
      val u = User.unsaved()
      val c = new Course()
      
      val appr = Approval(u.itself)
      
      val perm = Permissions.Read(c.itself)
      appr.permissions.add(perm)
      
      appr.ask(Permissions.Read(refCourse(c._id.stringify))) must_== Approved("Already approved").itself
    }    
    
    "cache delegated permissions" in {      
      val u = User.unsaved()
      val c = new Course()
      val e = ContentEntry.unsaved(course=c.itself, addedBy=u.itself)
      
      val approval = Approval(u.itself)      
      val approved = approval ask Permissions.ReadEntry(e)
      approval.permissions.contains(Permissions.Read(c.itself)) must_== true
    }       
    
  }

}