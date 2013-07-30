package com.impressory.auth.controllers

import play.api.libs.json.Json


/**
 * Remembers details about a user from a log-in service if that user has not logged in before.
 * (So that we can show the "register new account?" form)
 */
case class InterstitialMemory (
  service: String,
  id: String,
  username: Option[String],
  name: Option[String],
  nickname: Option[String],
  avatar: Option[String]
) {
  
  def toJsonString = InterstitialMemory.jsonFormat.writes(this).toString
}

object InterstitialMemory {
  
  implicit val jsonFormat = Json.format[InterstitialMemory]
  
}