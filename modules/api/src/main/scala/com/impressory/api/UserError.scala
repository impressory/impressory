package com.impressory.api

/** An error by the user. */
case class UserError(msg:String) extends Exception(msg)