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

class UserToJsonTest extends Specification {
      
  "UserToJson" should {
    
    "contain the identities" in {
      
      val u = User.unsaved()
      val i1 = Identity.unsaved(service="service1", value="value1", avatar=Some("avatar1"), username=Some("username1"))
      val i2 = Identity.unsaved(service="service2", value="value2", avatar=Some("avatar2"), username=Some("username2"))
      u.identities = Seq(i1, i2)
      
      val testsRan = for (
          json <- u.toJsonFor(Approval(u.itself));
          i = (json \ "identities")
      ) yield {

        val t1 = i(0).as[JsObject].-("id") must_== Json.parse("""
            { "service" : "service1", "value": "value1", "avatar": "avatar1", "username": "username1" } 
        """)
        
        val t2 = i(1).as[JsObject].-("id") must_== Json.parse("""
            { "service" : "service2", "value": "value2", "avatar": "avatar2", "username": "username2" } 
        """)
        
        t1 and t2
      }
      
      testsRan.toOption.get
    }
  }

}