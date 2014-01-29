package com.impressory

import com.wbillingsley.handy._
import play.api.libs.json._
import com.impressory.api._
import scala.language.implicitConversions;

package object json {
  
  import com.impressory.plugins.LookUps._
  
  implicit def refToJson[T <: HasStringId](ref:Ref[T]) = Json.toJson(ref.getId)
  
  implicit object writesRef extends Writes[Ref[HasStringId]] {
    def writes(r:Ref[HasStringId]) = Json.toJson(r.getId)
  }

  implicit def writesRefMany[T <: HasStringId, K] = new Writes[RefManyById[T, K]] {
    def writes(r:RefManyById[T, K]) = Json.toJson(r.getIds)
  }
  
  
  implicit def readsRef[T <: HasStringId](implicit lu:LookUp[T, String]) = new Reads[Ref[T]] {
    def reads(j:JsValue) = Reads.StringReads.reads(j).map(s => LazyId(s).of(lu))
  }

}