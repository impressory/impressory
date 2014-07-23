package com.impressory

/**
 * Created by wbillingsley on 23/07/2014.
 */
package object api {

  type User = com.wbillingsley.handy.user.User
  val User = com.wbillingsley.handy.user.User

  type Identity = com.wbillingsley.handy.user.Identity
  val Identity = com.wbillingsley.handy.user.Identity

  type ActiveSession = com.wbillingsley.handy.user.ActiveSession
  val ActiveSession = com.wbillingsley.handy.user.ActiveSession

  type PasswordLogin = com.wbillingsley.handy.user.PasswordLogin
  val PasswordLogin = com.wbillingsley.handy.user.PasswordLogin
}
