package com.impressory.play

/**
 * 
 */
package object model {
  
  import _root_.com.impressory.reactivemongo
  
  /*
   * For the moment, we're using package type aliases to direct
   * particular types to particular database implementations.
   * 
   * This is a temporary measure, as code is ported from the
   * previous structure (The Intelligent Book)
   */
  type User = reactivemongo.User
  val User = reactivemongo.User

  type Identity = reactivemongo.Identity
  val Identity = reactivemongo.Identity
  
  type Course = reactivemongo.Course
  val Course = reactivemongo.Course
  
  type Registration = reactivemongo.Registration
  val Registration = reactivemongo.Registration

  type ContentEntry = reactivemongo.ContentEntry
  val ContentEntry = reactivemongo.ContentEntry
  
  type ContentSequence = reactivemongo.ContentSequence
  val ContentSequence = reactivemongo.ContentSequence
  
  type WebPage = reactivemongo.WebPage
  val WebPage = reactivemongo.WebPage
  
}