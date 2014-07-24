package com.impressory.reactivemongo

import com.impressory.plugins.LookUps
import reactivemongo.bson.BSONObjectID

/**
 * Created by wbillingsley on 24/07/2014.
 */
object Plugin {

  def onStart() = {
    LookUps.contentEntryDAO = ContentEntryDAO
    LookUps.courseDAO = CourseDAO
    LookUps.userDAO = UserDAO
    LookUps.registrationDAO = RegistrationDAO
    LookUps.courseInviteDAO = CourseInviteDAO
    LookUps.chatCommentDAO = ChatCommentDAO

    LookUps.userProvider = UserDAO

    LookUps.idAllocator = Some(() => BSONObjectID.generate.stringify)
  }

}
