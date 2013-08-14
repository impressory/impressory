package com.impressory.play.json

import com.impressory.play.model._
import com.wbillingsley.handy._
import Ref._
import RefMany._
import play.api.libs.json._

import JsonConverters._

object UserToJson {
  
    /**
     * JSON representation for other users
     */
    def toJson(user:User) = Json.obj(
          "id" -> user.id,
          "name" -> user.name, 
          "nickname" -> user.nickname,
          "username" -> user.username,
          "avatar" -> user.avatar
        )
    
    /**
     * JSON representation for other users
     */
    def toJsonForSelf(user:User): Ref[JsValue] = {
      for (
        identities <- user.identities.toRefMany.toJson
      ) yield {
        toJson(user) ++ Json.obj(
          "email" -> user.email,
          "avatar" -> user.avatar,
          "passSet" -> user.pwhash.isDefined,
          "identities" -> identities
        )
      }
    }   
    
    def toJsonFor(user:User, appr:Approval[User]) = {
      if (appr.who == user.itself) {
        toJsonForSelf(user)
      } else {
        toJson(user).itself
      }
    }
    
    implicit class RUToJsonForSelf(val ru: Ref[User]) extends AnyVal {
      def toJsonForSelf = for (u <- ru; j <- UserToJson.toJsonForSelf(u)) yield j
    }
    
    implicit class UToJsonForSelf(val u: User) extends AnyVal {
      def toJsonForSelf = for (j <- UserToJson.toJsonForSelf(u)) yield j
    }
    
}

object IdentityToJson {
  
  def toJson(identity:Identity) = Json.obj(
    "id" -> identity.id,
    "service" -> identity.service,
    "value" -> identity.value,
    "username" -> identity.username,
    "avatar" -> identity.avatar
  )
  
  def toJsonFor(identity:Identity, appr:Approval[User]) = toJson(identity)
  
}

object RegistrationToJson {

  def toJson(reg: Registration): JsValue = {
    Json.obj(
      "course" -> reg.course.getId.map(_.stringify),
      "roles" -> reg.roles.map(_.toString),
      "created" -> reg.created,
      "updated" -> reg.updated
    )
  }
}