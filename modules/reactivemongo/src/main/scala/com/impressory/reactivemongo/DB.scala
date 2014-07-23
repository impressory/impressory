package com.impressory.reactivemongo

import com.wbillingsley.handy.{HasStringId, Ref, RefManyById, RefWithId }

import reactivemongo.bson._
import reactivemongo.api._

object DBConnector extends com.wbillingsley.handy.reactivemongo.DBConnector {
  
  dbName = "impressory"

}