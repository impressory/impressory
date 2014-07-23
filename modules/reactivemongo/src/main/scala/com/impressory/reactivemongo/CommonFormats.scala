package com.impressory.reactivemongo

import com.wbillingsley.handy.{Id, Ids}
import Id._
import Ids._
import com.impressory.api._
import reactivemongo.bson._

/**
 * Created by wbillingsley on 23/07/2014.
 */
object CommonFormats {

  def writeId[T](i:Id[T, String]):BSONObjectID = BSONObjectID(i.id)

  def writeId[T](oi:Option[Id[T, String]]):BSONValue = {
    val opt = for {
      i <- oi
    } yield BSONObjectID(i.id)
    opt.getOrElse(BSONNull)
  }

  def writeIds[T](ids:Ids[T, String]):BSONArray = BSONArray(for {
    id <- ids.ids
  } yield BSONObjectID(id))

  implicit def idHandler[T] = new BSONHandler[BSONObjectID, Id[T, String]] {
    def read(id:BSONObjectID) = {
      id.stringify.asId[T]
    }

    def write(i:Id[T, String]) = writeId(i)
  }

  implicit def idsHandler[T] = new BSONHandler[BSONArray, Ids[T, String]] {
    def read(ids:BSONArray) = {
      val strings = for {
        id <- ids.as[Seq[BSONObjectID]]
      } yield id.stringify
      strings.asIds[T]
    }

    def write(ids:Ids[T, String]) = writeIds(ids)
  }

  implicit val userIdHandler = idHandler[User]
  implicit val userIdsHandler = idsHandler[User]
  implicit val courseIdHandler = idHandler[Course]
  implicit val courseIdsHandler = idsHandler[Course]

  implicit val upDownVoting = Macros.handler[UpDownVoting]

  implicit val embeddedComments = Macros.handler[EmbeddedComment]

  implicit val comments = Macros.handler[Comments]




}
