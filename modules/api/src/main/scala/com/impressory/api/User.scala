package com.impressory.api

import com.wbillingsley.handy.HasStringId
import com.wbillingsley.handy.appbase.User._

case class User(

    id:String,
    
    name:Option[String] = None,
    
    nickname:Option[String] = None,
    
    avatar:Option[String] = None,
    
    pwlogin: PasswordLogin = PasswordLogin(),
    
    identities:Seq[Identity] = Seq.empty,
    
    activeSessions:Seq[ActiveSession] = Seq.empty,
    
    registrations:Seq[Registration] = Seq.empty,
    
    siteRoles: Set[SiteRole] = Set(SiteRole.Reader, SiteRole.Author),

    created: Long = defaultCreated
  
) extends com.wbillingsley.handy.appbase.User[Identity, PasswordLogin] with HasStringId {

  /**
   * Two users are equal if they have the same ID
   */
  override def equals(obj: Any) = {
    obj.isInstanceOf[User] &&
      obj.asInstanceOf[User].id == id
  }

}

/**
 * A representation of a social login, such as a GitHub account.
 * 
 * This inherits some functionality from appbase
 */
case class Identity (
    
    service: String,
    
    value: String, 
    
    avatar: Option[String] = None,
    
    username: Option[String] = None,
    
    since: Long = com.wbillingsley.handy.appbase.Identity.defaultSince
    
) extends com.wbillingsley.handy.appbase.Identity


/**
 * The details to log a user in by username or email and password
 */
case class PasswordLogin (
    
    salt: Option[String] = com.wbillingsley.handy.appbase.PasswordLogin.defaultSalt,
    
    pwhash: Option[String] = None,
    
    username: Option[String] = None,
    
    email:Option[String] = None
    
) extends com.wbillingsley.handy.appbase.PasswordLogin


/**
 * The user's active sessions are kept, for resolving identity and in order to support
 * remote log-out.
 */
case class ActiveSession(
    
    /** Unique identifier, kept in the session cookie */
    key:String, 
    
    /** IP address of the user, when this session was created */
    ip:String,
    
    /** When this session was created */
    since:Long = System.currentTimeMillis
)