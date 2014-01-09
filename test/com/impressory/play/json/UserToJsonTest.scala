package com.impressory.play.json

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.wbillingsley.handy._
import Ref._
import com.impressory.api._
import com.impressory.play.model._
import play.api.libs.json._
import com.impressory.json._

class UserToJsonTest extends Specification {
      
  "UserToJson" should {
    
    "contain the identities" in {
      
      val i1 = new Identity(service="service1", value="value1", avatar=Some("avatar1"), username=Some("username1"))
      val i2 = new Identity(service="service2", value="value2", avatar=Some("avatar2"), username=Some("username2"))
      val u = new User(id="1", identities = Seq(i1, i2))
      
      val testsRan = for (
          json <- UserToJson.toJsonFor(u, Approval(u.itself));
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