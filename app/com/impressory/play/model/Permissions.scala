package com.impressory.play.model

import com.wbillingsley.handy._
import Ref._
import com.impressory.api._

/**
 * Translates from SecurityModel in the previous version
 */
object Permissions {
  
  /**
   * Create a course
   */
  case object CreateCourse extends Perm[User] {    
    def resolve(prior:Approval[User]) = {
      (for (r <- prior.who if r.siteRoles contains SiteRole.Author) yield {
        Approved("You are an Author on this site")
      }) orIfNone Refused("You must be an Author on this site to create books.")
    }
  }  
  
  
  case class EditCourse(course:Ref[Course]) extends PermOnIdRef[User, Course](course) {
    def resolve(prior:Approval[User]) = hasRole(course, prior.who, CourseRole.Administrator)
  }
  
  /**
   * Read a book or an entry in a book
   * @param bookRef
   */
  case class Read(course:Ref[Course]) extends PermOnIdRef[User, Course](course) {
    def resolve(prior:Approval[User]) = {
      course flatMap { b =>
        b.signupPolicy match {
	        case CourseSignupPolicy.open => Approved("Anyone may read this book").itself
	        case CourseSignupPolicy.loggedIn => {
	          (prior.who map {
	            w => Approved("Logged in readers may read this book")
	          }) orIfNone Refused("You must log in to read this book")
	        }
	        case _ => hasRole(course, prior.who, CourseRole.Reader)          
        }
      } orIfNone Refused("Book not found")
    }
  }  

  case class ReadEntry(entry:Ref[ContentEntry]) extends PermOnIdRef[User, ContentEntry](entry) {
    def resolve(prior:Approval[User]) = prior ask Read(entry flatMap(_.course))  
  }
  
  /**
   * Add content to a course
   * @param bookRef
   */
  case class AddContent(course:Ref[Course]) extends PermOnIdRef[User, Course](course) {
    def resolve(prior:Approval[User]) = hasRole(course, prior.who, CourseRole.Author)
  }  
  
  /**
   * Protect content or edit protected content in a book
   * @param bookRef
   */
  case class ProtectContent(course:Ref[Course]) extends PermOnIdRef[User, Course](course) {
    def resolve(prior:Approval[User]) = hasRole(course, prior.who, CourseRole.Moderator)
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
       r <- u.registrations.find(_._course == c.id)
    ) yield r.roles
  }
  
  def hasRole(course:Ref[Course], user:Ref[User], role:CourseRole):Ref[Approved] = {
    (
      for (
        roles <- getRoles(course, user) if roles.contains(role)
      ) yield Approved(s"You have role $role for this course")
    ) orIfNone Refused(s"You do not have role $role for this course")
  }
  

}