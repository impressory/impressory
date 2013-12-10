package com.impressory.reactivemongo

import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable._
import org.specs2.specification.Fragments
import org.specs2.specification.Step

trait DatabaseSpec extends Specification {
  lazy val dbSetup = SpecSetup
  override def map(fs: =>Fragments) = Step(dbSetup.doSetup) ^ fs
}


object SpecSetup {

  lazy val doSetup = {
    DBConnector.dbName = "testImpressory"
    DBConnector.db.drop()
    true
  }
  
}