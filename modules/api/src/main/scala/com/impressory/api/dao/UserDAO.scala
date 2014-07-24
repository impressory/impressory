package com.impressory.api.dao

import com.impressory.api.{Course, Registration, User}
import com.wbillingsley.handy.{Id, RefFailed, LookUp}

trait UserDAO {

  def lookUp:LookUp[User,String]

}

object NullUserDAO extends UserDAO {

  private val msg = "No UserDAO has been configured"

  private def failed = RefFailed(new IllegalStateException(msg))

  def lookUp = LookUp.fails(msg)

}
