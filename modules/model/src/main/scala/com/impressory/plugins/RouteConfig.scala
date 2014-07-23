package com.impressory.plugins

import com.wbillingsley.handyplay.DataActionConfig
import play.api.mvc._

object RouteConfig {

  /**
   * Specifies the HTML home, etc. The value is set on start-up (in the webapp), but the variable is
   * defined here so that modules can import it
   */
  implicit var dataActionConfig:DataActionConfig = new DataActionConfig {
    
    def homeAction = Action { Results.InternalServerError("Home action has not been configured yet") }
    
    def errorCodeMap = Map.empty
    
  }
  
}