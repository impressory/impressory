package com.impressory

import com.wbillingsley.handy.{HasStringId, Ref, RefManyById, LazyId}
import play.api.libs.json._
import com.impressory.api._

package object json {
  
  import scala.language.implicitConversions;
  implicit def refToJson[T <: HasStringId](ref:Ref[T]) = Json.toJson(ref.getId)
  
  implicit object writesRef extends Writes[Ref[HasStringId]] {
    def writes(r:Ref[HasStringId]) = Json.toJson(r.getId)
  }

  implicit def writesRefMany[T <: HasStringId, K] = new Writes[RefManyById[T, K]] {
    def writes(r:RefManyById[T, K]) = Json.toJson(r.getIds)
  }
  
  
  def readsRef[T <: HasStringId](c:Class[T]) = new Reads[Ref[T]] {
    def reads(j:JsValue) = Reads.StringReads.reads(j).map(s => new LazyId(c, s))
  }
  
  implicit val readsRefUser = readsRef(classOf[User])
  
}