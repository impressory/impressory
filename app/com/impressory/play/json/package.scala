package com.impressory.play

import play.api.libs.json.{Writes, JsString, JsNull}
import reactivemongo.bson.BSONObjectID
import com.impressory.reactivemongo.HasBSONId
import com.wbillingsley.handy.{Ref, LazyId}

package object json {
  
  implicit object bsonWrites extends Writes[BSONObjectID] {
    def writes(id:BSONObjectID) = JsString(id.stringify)
  }
  
  implicit def RefBSONWrites[T <: HasBSONId] = new Writes[Ref[T]] {
    def writes(r:Ref[T]) = {
      val str = r.getId(HasBSONId.GetsBSONId).map(_.stringify)
      str.map(JsString(_)).getOrElse(JsNull)
    }
  }  

}