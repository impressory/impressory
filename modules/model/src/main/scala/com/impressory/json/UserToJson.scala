package com.impressory.json

import com.impressory.api._
import com.wbillingsley.handy._
import com.wbillingsley.handy.Ref._
import com.wbillingsley.handy.RefMany._
import play.api.libs.json._
import com.wbillingsley.handy.appbase.JsonConverter
import com.wbillingsley.handy.appbase.JsonConverter._
import play.api.libs.json.Json.toJsFieldJsValueWrapper

object UserToJson extends JsonConverter[User, User] {
  
    /**
     * JSON representation for other users
     */
    def toJson(user:User) = Json.obj(
          "id" -> user.id,
          "name" -> user.name, 
          "nickname" -> user.nickname,
          "avatar" -> user.avatar
        ).itself
    
    /**
     * JSON representation for other users
     */
    def toJsonForSelf(user:User): Ref[JsValue] = {
      
      val i = for {
        identity <- user.identities.toRefMany
        j <- IdentityToJson.toJson(identity)
      } yield j
      
      for {
        j <- toJson(user)  
        identities <- i.toRefOne
      } yield {
         j ++ Json.obj(
          "email" -> user.pwlogin.email,
          "avatar" -> user.avatar,
          "passSet" -> user.pwlogin.pwhash.isDefined,
          "identities" -> identities.toSeq
        )
      }
    }   
    
    def toJsonFor(user:User, appr:Approval[User]) = {
      if (appr.who == user.itself) {
        toJsonForSelf(user)
      } else {
        toJson(user)
      }
    }
    
    implicit class RUToJsonForSelf(val ru: Ref[User]) extends AnyVal {
      def toJsonForSelf = for (u <- ru; j <- UserToJson.toJsonForSelf(u)) yield j
    }
    
    implicit class UToJsonForSelf(val u: User) extends AnyVal {
      def toJsonForSelf = for (j <- UserToJson.toJsonForSelf(u)) yield j
    }
    
}

object IdentityToJson extends JsonConverter[Identity, User] {
  
  def toJson(identity:Identity) = Json.obj(
    "service" -> identity.service,
    "value" -> identity.value,
    "username" -> identity.username,
    "avatar" -> identity.avatar
  ).itself
  
  def toJsonFor(identity:Identity, appr:Approval[User]) = toJson(identity)
  
}

object RegistrationToJson extends JsonConverter[Registration, User] {

  def toJson(reg: Registration) = {
    Json.obj(
      "course" -> reg.course,
      "roles" -> reg.roles.map(_.toString),
      "created" -> reg.created,
      "updated" -> reg.updated
    ).itself
  }
  
  def toJsonFor(r:Registration, appr:Approval[User]) = toJson(r)
}