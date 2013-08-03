package com.impressory.play

import play.api.mvc.Request

package object controllers {
  
  /**
   * Implicit class making it easy to generate an Approval token or a 
   * reference to the logged in User from the request.
   */
  implicit class RequestUser[AC](val request:Request[AC]) extends AnyVal {
    
    /**
     * A reference to the logged in user.
     */
    def user = RequestUtils.loggedInUser(request.session)
   
    /**
     * An Approval token for the logged in user (which may be none).
     */
    def approval = RequestUtils.approval(request.session)
    
    /**
     * The sessionKey in the cookie
     */
    def sessionKey = RequestUtils.sessionKey(request.session)
  }  

}