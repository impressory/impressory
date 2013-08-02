package com.impressory.reactivemongo

import com.wbillingsley.encrypt.Encrypt
import com.wbillingsley.handy._
import Ref._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import com.impressory.api._

import play.api.libs.concurrent.Execution.Implicits._

class User(

  var username: Option[String],

  var salt: Option[String] = Some(Encrypt.genSaltB64),

  var pwhash: Option[String] = None,

  var email: Option[String] = None,

  var name: Option[String] = None,

  var nickname: Option[String] = None,

  var identities: Seq[Identity] = Seq.empty,

  var registrations: Seq[Registration] = Seq.empty,

  var siteRoles: Set[SiteRole] = Set(
    SiteRole.Reader,
    SiteRole.Author),

  var avatar: Option[String] = None,

  val created: Long = System.currentTimeMillis,

  val _id: BSONObjectID = BSONObjectID.generate) extends HasBSONId with CanSendToClient {

  /**
   * Two users are equal if they have the same ID
   */
  override def equals(obj: Any) = {
    obj.isInstanceOf[User] &&
      obj.asInstanceOf[User].id == id
  }

  def id = _id

  def hash(password: String) = for (s <- salt) yield Encrypt.encrypt(s, password)

  def save = User.save(this)

}

object User extends FindById[User] {

  val honorifics = Seq("rev. ", "hon. ", "rt. hon. ", "h.r.h. ", "gen. ", "flt. lt. ", "adm. ", "dr. ", "prof. ")

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

  implicit object bsonWriter extends BSONDocumentWriter[User] {
    def write(user: User) = BSONDocument(
      "_id" -> user._id,
      "username" -> user.username,
      "name" -> user.name,
      "nickname" -> user.nickname,
      "salt" -> user.salt,
      "pwhash" -> user.pwhash,
      "email" -> user.email,
      "identities" -> user.identities,
      "registrations" -> user.registrations,
      "siteRoles" -> user.siteRoles,
      "avatar" -> user.avatar,
      "created" -> user.created)
  }

  implicit object bsonReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      val user = new User(
        _id = doc.getAs[BSONObjectID]("_id").get,
        name = doc.getAs[String]("name"),
        username = doc.getAs[String]("username"),
        nickname = doc.getAs[String]("nickname"),
        salt = doc.getAs[String]("salt"),
        pwhash = doc.getAs[String]("pwhash"),
        email = doc.getAs[String]("email"),
        avatar = doc.getAs[String]("avatar"),
        created = doc.getAs[Long]("created").getOrElse(System.currentTimeMillis),
        identities = doc.getAs[Seq[Identity]]("identities").getOrElse(Seq.empty),
        registrations = doc.getAs[Seq[Registration]]("registrations").getOrElse(Seq.empty),
        siteRoles = doc.getAs[Set[SiteRole]]("siteRoles").get //new RefManyById(classOf[Identity], doc.getAs[BSONArray]("identities").get.values.flatMap(v => v.seeAsOpt[BSONObjectID]).toSeq)
        )

      user
    }
  }

  val collName = "user"

  def byName(name: String) = {
    val query = BSONDocument("name" -> BSONString(name))
    val coll = DB.coll(collName)
    val cursor = coll.find(query).cursor[User]
    cursor.enumerate
  }

  def byUsername(name: String) = {
    val query = BSONDocument("username" -> BSONString(name))
    val coll = DB.coll(collName)
    val cursor = coll.find(query).one[User]
    new RefFutureRef(cursor.map(Ref(_)))
  }

  def byEmail(email: String) = {
    val query = BSONDocument("email" -> BSONString(email))
    val coll = DB.coll(collName)
    val cursor = coll.find(query).one[User]
    new RefFutureRef(cursor.map(Ref(_)))
  }

  def byIdentity(service: String, value: String) = {
    val query = BSONDocument("identities.key" -> BSONDocument("service" -> BSONString(service), "value" -> BSONString(value)))

    println(query)

    val coll = DB.coll(collName)
    val cursor = coll.find(query).one[User]
    new RefFutureRef(cursor.map(Ref(_)))
  }

  def createByEmail(email: String, password: String) = {
    val u = new User(username = None, email = Some(email), nickname = Some(defaultEmailNickname(email)))
    u.pwhash = u.hash(password)
    save(u)
  }

  def unsaved(
    username: Option[String] = None, password: Option[String] = None,
    name: Option[String] = None, email: Option[String] = None,
    nickname: Option[String] = None, avatar: Option[String] = None) = {

    val u = new User(username = username, name = name, nickname = nickname, email = email, avatar=avatar)
    u.pwhash = password.flatMap(p => u.hash(p))
    u
  }

  def save(user: User): Ref[User] = {
    import com.impressory.api.SiteRole;
    import com.impressory.api.UserError
    import reactivemongo.core.commands.LastError

    //val t = System.nanoTime()
    val fle = DB.coll(collName).update(selector = BSONDocument("_id" -> user._id), update = user, upsert = true)
    //val t2 = System.nanoTime()
    //println("time2-1 " + (t2 - t))

    fle.recover {
      case _ =>
      //val t3 = System.nanoTime()
      //println("time3-1 " + (t3 - t))      
    }

    val fut = fle.map { _ => user.itself } recover {
      case l: LastError => {
        if (l.code == Some(11000)) { RefFailed(UserError("Whoops, that one's already taken")) } else RefFailed(l)
      }
    }
    new RefFutureRef(fut)
  }
  
  def saveNew(user: User) = saveSafe(bsonWriter.write(user), user)
  
  def register(u: Ref[User], c:Ref[Course], roles:Set[CourseRole]) = {
    import HasBSONId._
    
    val coll = DB.coll(collName)
    val reg = new Registration(course=c, roles=roles)
    
    val ffle = for (opt <- coll.find(BSONDocument("_id" -> u, "registrations.course" -> c)).one[User]) yield {
      val fle = if (opt.isEmpty) {
        // User is not registered for this course (or does not exist)
        val query = BSONDocument("_id" -> u)
        val update = BSONDocument("$push" -> BSONDocument("registrations" -> reg))
        coll.update(query, update)
      } else {
        // User is registered for this course
        import Registration._
        
        val query = BSONDocument("_id" -> u, "registrations.course" -> c)
        val update = BSONDocument("$addToSet" -> BSONDocument("registrations.$.roles" -> BSONDocument("$each" -> roles)))
        coll.update(query, update)
      }
      fle
    } 
    val fle = ffle.flatMap(fle => fle)
    
    val rfr = fle map { _ => reg.itself } recover {
      case l:LastError => {
        RefFailed(l)
      }
    }
    new RefFutureRef(rfr)
  }

}
