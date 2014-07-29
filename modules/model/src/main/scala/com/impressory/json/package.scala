package com.impressory

import com.wbillingsley.handy._
import Id._
import Ids._
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json._
import com.impressory.api._
import scala.language.implicitConversions;

package object json {
  
  import com.impressory.plugins.LookUps._

  implicit def writesId[T] = new Writes[Id[T, String]] {
    def writes(r:Id[T, String]) = Json.toJson(r.id)
  }

  implicit def readsId[T] = new Reads[Id[T, String]] {
    def reads(j:JsValue) = Reads.StringReads.reads(j).map(s => s.asId[T])
  }

  implicit def readsIds[T] = new Reads[Ids[T, String]] {
    def reads(j:JsValue) = Reads.ArrayReads[String].reads(j).map(s => s.toSeq.asIds[T])
  }

  implicit def writesIds[T] = new Writes[Ids[T, String]] {
    def writes(r:Ids[T, String]) = Json.toJson(r.ids)
  }

  implicit def writesRef[T <: HasStringId[T], _] = new Writes[RefWithId[T]] {
    def writes(r:RefWithId[T]) = Json.toJson(r.getId)
  }

  implicit def readsRef[T <: HasStringId[T]](implicit lu:LookUp[T, String]) = new Reads[Ref[T]] {
    def reads(j:JsValue) = Reads.StringReads.reads(j).map(s => LazyId(s).of(lu))
  }

}