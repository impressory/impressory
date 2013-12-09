package com.impressory.play.controllers

import play.api._
import play.api.mvc._

import com.wbillingsley.handy._
import Ref._

import com.impressory.api._


object RequestUtils {

  
  /*----
   * Form utilities
   *----*/ 
  
  /**
   * Gets a trimmed Option[String] value from a map of parameters
   * @param map
   * @param key
   * @return
   */
  def fetchStr(map:Map[String,Seq[String]], key:String) = {
    val v = map.getOrElse(key, Seq.empty[String]).headOption
    v.flatMap{str =>
      val trim = str.trim()
      if (trim.isEmpty()) { None } else { Some(trim) }
    }
  }

  /**
   * Gets a Set value from a map of parameters
   * @param map
   * @param key
   * @return
   */
  def fetchSet(map:Map[String,Seq[String]], key:String) = {
    map.getOrElse(key, Seq.empty[String]).toSet
  }

  /**
   * Gets a Boolean value from a map of parameters
   * @param map
   * @param key
   * @return
   */
  def fetchBool(map:Map[String,Seq[String]], key:String) = {
    map.getOrElse(key, Seq.empty[String]).headOption.map { v =>
      parseBool(v)
    }
  }
  
  def parseBool(v:String):Boolean = {
      val str = v.toLowerCase
      str == "on" || str == "true" || str == "1"    
  }

  /**
   * Gets an Option[Int] value from a map of parameters
   * @param map
   * @param key
   * @return
   */
  def fetchInt(map:Map[String,Seq[String]], key:String) = {
    map.getOrElse(key, Seq.empty[String]).headOption.map(_.toInt)
  }

  def fetchSetInts(map:Map[String,Seq[String]], key:String) = {
    map.getOrElse(key, Seq.empty[String]).map(_.toInt).toSet
  }  
  
}