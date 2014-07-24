package com.impressory.api.dao

import com.impressory.api.events.ChatComment
import com.impressory.api.{Course, ContentEntry}
import com.wbillingsley.handy._

trait ChatCommentDAO {

  def lookUp:LookUp[ChatComment,String]

}

object NullChatCommentDAO extends ChatCommentDAO {

  private val msg = "No ChatCommentDAO has been configured"

  private def failed = RefFailed(new IllegalStateException(msg))

  def lookUp = LookUp.fails(msg)

}