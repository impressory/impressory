package com.impressory.security

import com.impressory.plugins.LookUps
import com.wbillingsley.handy._
import Ref._
import com.impressory.api._

import com.impressory.plugins.LookUps._

/**
 * Translates from SecurityModel in the previous version
 */
object Permissions {

  /**
   * Create a course
   */
  val createCourse = Perm.unique[User] { case (prior) =>
    (for (r <- prior.who) yield {
      Approved("Registered users may create courses")
    }) orIfNone Refused("You must be logged in to create courses.")
  }

  val editCourse = Perm.onId[User, Course, String] { case (prior, course) =>
    hasRole(course, prior.who, CourseRole.Administrator, prior.cache)
  }

  val readCourse = Perm.onId[User, Course, String] { case (prior, course) =>
    prior.cache[Course, String](course) flatMap { b =>
      b.settings.signupPolicy match {
        case CourseSignupPolicy.open => Approved("Anyone may read this book").itself
        case CourseSignupPolicy.loggedIn => {
          (prior.who map {
            w => Approved("Logged in readers may read this book")
          }) orIfNone Refused("You must log in to read this book")
        }
        case _ => hasRole(course, prior.who, CourseRole.Reader, prior.cache)
      }
    } orIfNone Refused("Book not found")
  }
  
  /**
   * Read a book or an entry in a book
   */
  val chat = Perm.onId[User, Course, String] { case(prior, course) =>
    prior.cache[Course, String](course) flatMap { c =>
    c.settings.chatPolicy match {
      case CourseChatPolicy.allReaders => prior ask readCourse(c.itself)
      case _ => hasRole(c.itself, prior.who, CourseRole.Chatter, prior.cache)
      }
    } orIfNone Refused("Course not found")
  }

  /**
   * Up or Down vote an entry in a course
   */
  val voteOnEntry = Perm.onId[User, ContentEntry, String] { case (prior, entry) =>
    for (
      who <- prior.who orIfNone Refused("You must be logged in to vote");
      e <- entry orIfNone Refused("Entry not found");
      a <- {
        if (e.voting.hasVoted(who.itself)) {
          RefFailed(Refused("You have already voted"))
        } else {
          prior ask chat(e.course)
        }
      }
    ) yield a
  }
  
  /**
   * Up or Down vote an entry in a course
   */
  val commentOnEntry = Perm.onId[User, ContentEntry, String] { case (prior, entry) =>
    for (
      e <- entry orIfNone Refused("Entry not found");
      a <- prior ask chat(e.course)
    ) yield a
  }
  
  val readEntry = Perm.onId[User, ContentEntry, String] { case(prior, entry) =>
    for (
      e <- prior.cache[ContentEntry, String](entry);
      p <- prior ask readCourse(e.course)
    ) yield p
  }
  
  /**
   * Add content to a course
   */
  val addContent = Perm.onId[User, Course, String] { case (prior, course) =>
    hasRole(course, prior.who, CourseRole.Author, prior.cache)
  }  
  
  /**
   * Protect content or edit protected content in a book
   */
  val protectContent = Perm.onId[User, Course, String] { case (prior, course) =>
    hasRole(course, prior.who, CourseRole.Moderator, prior.cache)
  }  
  
  /**
   * Protect content or edit protected content in a book
   */
  val editUnprotectedContent = Perm.onId[User, Course, String] { case (prior, course) =>
    hasRole(course, prior.who, CourseRole.Author, prior.cache)
  }  

  /**
   * Edit a content entry
   */
  val editContent = Perm.onId[User, ContentEntry, String] { case (prior, entry) =>
    prior.cache[ContentEntry, String](entry) flatMap { e =>
      if (e.settings.protect) {
        prior ask protectContent(e.course)
      } else {
        prior ask editUnprotectedContent(e.course)
      }
    } orIfNone Refused("Entry not found")
  }

  /**
   * View, edit, or create invites to a course
   */
  val manageCourseInvites = Perm.onId[User, Course, String] { case(prior, course) =>
    hasRole(course, prior.who, CourseRole.Administrator, prior.cache)
  }  
  
  val registerUsingInvite = Perm.onId[User, Course, String] { case(prior, course) =>
    (for (u <- prior.who) yield Approved("Anyone may register using an invite")) orIfNone Refused("You are not logged in")
  }  

  /*-------------------------
  * Invitation and registration methods
  */

  def useInviteByCourseAndCode(u:Ref[User], c:Ref[Course], code:String) = {
    // useInvite(u, BookInvitationDAO.inviteByBookAndCode(bookRef, code))
  }
    
  /*def useInvite(u:Ref[Course], invite:Ref[BookInvitation]) = {
    reader flatMap { r => invite flatMap { i =>
      BookInvitationDAO.useInvite(r, i)
    }}
  }*/

  def getRoles(course: Ref[Course], user: Ref[User]) = {
    for (
       c <- course;
       u <- user;
       r <- LookUps.registrationDAO.byUserAndCourse(u.id, c.id)
    ) yield r.roles
  }
  
  def hasRole(course:Ref[Course], user:Ref[User], role:CourseRole, cache:LookUpCache):Ref[Approved] = {
    (
      for (
        roles <- getRoles(cache(course), user) if roles.contains(role)
      ) yield Approved(s"You have role $role for this course")
    ) orIfNone Refused(s"You do not have role $role for this course")
  }
  

}