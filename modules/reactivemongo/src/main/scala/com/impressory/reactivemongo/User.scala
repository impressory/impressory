package com.impressory.reactivemongo

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import com.wbillingsley.handy.{Id, RefFuture, Ref, RefWithId}
import com.wbillingsley.handy.reactivemongo.DAO
import Ref._
import Id._
import com.impressory.api._
import com.wbillingsley.handyplay.UserProvider

import CommonFormats._

// Import the configuration to create RefByIds (where to look them up)
import com.impressory.plugins.LookUps._

object UserDAO extends DAO with UserProvider[User] with com.impressory.api.dao.UserDAO {

  type DataT = User
  
  val honorifics = Seq("rev. ", "hon. ", "rt. hon. ", "h.r.h. ", "gen. ", "flt. lt. ", "adm. ", "dr. ", "prof. ")

  val defaultSiteRoles = Set(SiteRole.Reader, SiteRole.Author)
  
  def defaultEmailNickname(email: String) = {
    def name = email.takeWhile(_ != '@')
    honorifics(scala.util.Random.nextInt(honorifics.length)) + name
  }

  implicit object bsonSiteRoleWriter extends BSONWriter[SiteRole, BSONString] {
    def write(sr: SiteRole) = BSONString(sr.toString)
  }

  implicit object bsonSiteRoleReader extends BSONReader[BSONString, SiteRole] {
    def read(s: BSONString) = SiteRole.valueOf(s.value)
  }
  
  implicit object bsonCourseRoleWriter extends BSONWriter[CourseRole, BSONString] {
    def write(sr:CourseRole) = BSONString(sr.toString)
  }
  
  implicit object bsonCourseRoleReader extends BSONReader[BSONString, CourseRole] {
    def read(s:BSONString) = CourseRole.valueOf(s.value)
  }  
  
  /** Converts PasswordLogin to and from BSON */
  implicit val pwloginFormat = Macros.handler[PasswordLogin]
  
  /** Converts Identity to and from BSON */
  implicit val identityFormat = IdentityHandler

  /** Converts ActiveSession to and from BSON */
  implicit val activeSessionFormat = Macros.handler[ActiveSession]  

  implicit object bsonReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      val user = new User(
        id = doc.getAs[Id[User,String]]("_id").get,
        name = doc.getAs[String]("name"),
        nickname = doc.getAs[String]("nickname"),
        avatar = doc.getAs[String]("avatar"),
        pwlogin = doc.getAs[PasswordLogin]("pwlogin").getOrElse(PasswordLogin()),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        identities = doc.getAs[Seq[Identity]]("identities").getOrElse(Seq.empty)
      )
      user
    }
  }

  val collName = "user"
    
  val db = DBConnector
  
  val clazz = classOf[User]

  val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def createByEmail(email: String, password: String) = {
    val u = User(id=allocateId.asId[User])
    val updated = u.copy(
      nickname=Some(defaultEmailNickname(email)),
      pwlogin=PasswordLogin(email=Some(email), pwhash = Some(u.pwlogin.hash(password)))
    ) 
    saveNew(updated)
  }

  /**
   * Saves the user's details
   */
  def saveDetails(u:User) = updateAndFetch(
    query=BSONDocument(idIs(u.id)), 
    update=BSONDocument("$set" -> BSONDocument(
      "name" -> u.name,
      "nickname" -> u.nickname,
      "avatar" -> u.avatar,
      "created" -> u.created
    ))
  )

  /**
   * Updates the user's login details
   */
  def updatePWLogin(u:User) = updateAndFetch(
    query=BSONDocument(idIs(u.id)), 
    update=BSONDocument("$set" -> BSONDocument(
      "pwlogin" -> u.pwlogin
    ))
  )
  
  /**
   * Set's the user's password
   */
  def setPassword(u:User, password:String) = {
    updatePWLogin(u.copy(pwlogin=u.pwlogin.copy(pwhash=Some(u.pwlogin.hash(password)))))
  }

  
  /**
   * Save a new user. This should only be used for new users because it overwrites
   * sessions and identities.
   */
  def saveNew(u:User) = saveSafe(
    BSONDocument(
      idIs(u.id),
      "name" -> u.name,
      "nickname" -> u.nickname,
      "avatar" -> u.avatar,
      "pwlogin" -> u.pwlogin,
      "identities" -> u.identities,
      "activeSessions" -> u.activeSessions,
      "created" -> u.created
    ),
    u
  )  
  
  /**
   * Adds an identity to this user
   */
  def pushIdentity(ru:RefWithId[User], i:Identity) = {
    updateAndFetch(
        query = BSONDocument("_id" -> ru.getId),
        update = BSONDocument("$push" -> BSONDocument("identities" -> i)) 
    )
  }  
  
  /**
   * Adds an identity to this user
   */
  def deleteIdentity(ru:Ref[User], service:String, id:String) = {
    for {
      id <- ru.refId
      updated <- updateAndFetch(
        query = BSONDocument(idIs(id)),
        update = BSONDocument("$pull" -> BSONDocument("identities" -> BSONDocument("service" -> service, "id" -> id)))
      )
    } yield updated
  }
  
  /** Adds a session to this user. Typically this happens at login. */
  def pushSession(ru:RefWithId[User], as:ActiveSession) = updateAndFetch(
    query = BSONDocument("_id" -> ru.getId),
    update = BSONDocument("$push" -> BSONDocument("activeSessions" -> as))
  )
  
  def deleteSession(ru:Ref[User], as:ActiveSession) = {
    for {
      userId <- ru.refId
      updated <- updateAndFetch(
        query = BSONDocument(idIs(userId)),
        update = BSONDocument("$pull" -> BSONDocument("activeSessions" -> BSONDocument("key" -> as.key)))
      )
    } yield updated
  }
  
  def bySessionKey(sessionKey:String):Ref[User] = {
    findOne(query=BSONDocument("activeSessions.key" -> sessionKey))
  }
  
  def byIdentity(service:String, id:String):Ref[User] = {
    findOne(query=BSONDocument("identities.key" -> BSONDocument("service" -> service, "value" -> id)))
  }
  

  def byName(name: String) = findMany(BSONDocument("name" -> BSONString(name)))
  
  def byUsername(u:String) = findOne(BSONDocument("pwlogin.username" -> u))
  
  def byEmail(e:String) = findOne(BSONDocument("pwlogin.email" -> e))  
  
  def byUsernameAndPassword(username:String, password:String) = {
    for (
      user <- byUsername(username) if {
       val hash = user.pwlogin.hash(password)
       hash == user.pwlogin.pwhash
      }      
    ) yield user
  }

  def byEmailAndPassword(email:String, password:String) = {
    for (
      user <- byEmail(email) if {
       val hash = user.pwlogin.hash(password)
       hash == user.pwlogin.pwhash
      }      
    ) yield user
  }

}

object IdentityHandler extends BSONDocumentReader[Identity] with BSONDocumentWriter[Identity] {
  
  def write(identity:Identity) = BSONDocument(
      "key" -> BSONDocument(
        "service" -> identity.service,
        "value" -> identity.value
      ),    	
      "username" -> identity.username,
      "avatar" -> identity.avatar,
      "since" -> identity.since
    )        
  
  def read(doc:BSONDocument):Identity = {      
      val key = doc.getAs[BSONDocument]("key").get
      Identity(
        username = doc.getAs[String]("username"),
        service = key.getAs[String]("service").get,
        value = key.getAs[String]("value").get,
        since = doc.getAs[Long]("since").get,
        avatar = doc.getAs[String]("avatar")
      )
  }

}
