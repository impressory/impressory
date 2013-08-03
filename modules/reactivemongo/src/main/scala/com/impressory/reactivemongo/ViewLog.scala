package com.impressory.reactivemongo

import com.wbillingsley.handy._
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.GetLastError

object ViewLog {

  case class Record(
    course: Ref[Course],
      
    user: Ref[User],
    
    session: Option[String],
    
    entry: Option[ContentEntry],
    
    template: Option[String],
    
    params: Map[String, String],
    
    how: Option[String]
  )

  val collectionName = "viewLog"
  
  val ONE_HOUR:Long = 1000 * 60 * 60
  val ONE_DAY:Long = 1000 * 60 * 60 * 24
  
  
    
  def log(record:Record) = {
    
    val time = System.currentTimeMillis()    
    val withinDay = time % ONE_DAY    
    val day = time - withinDay
    val hour = withinDay / ONE_HOUR
    
    val query = BSONDocument("course" -> record.course, "day" -> day)
    
    def escapeFieldName(str:String) = str.replace("\\", "\\\\").replace(".", "\\≥").replace("$", "\\¢")
    
    val incParams = for ((key, value) <- record.params) yield (s"hourly.${hour}.params.${key}.${value}" -> BSONInteger(1))
    
    val incTotal = (
      s"hourly.${hour}.total" -> BSONInteger(1)
    )
    val incUser:Seq[(String, BSONInteger)] = for (id <- record.user.getId.toSeq) yield (s"hourly.${hour}.user.${id}" -> BSONInteger(1))
    val incSession:Seq[(String, BSONInteger)] = for (id <- record.session.toSeq) yield (s"hourly.${hour}.session.${escapeFieldName(id)}" -> BSONInteger(1))
    val incTemplate:Seq[(String, BSONInteger)] = for (id <- record.template.toSeq) yield (s"hourly.${hour}.template.${escapeFieldName(id)}" -> BSONInteger(1))
    val incHow:Seq[(String, BSONInteger)] = for (id <- record.how.toSeq) yield (s"hourly.${hour}.how.${escapeFieldName(id)}" -> BSONInteger(1))
    
    val incSites:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v = e.site) yield (s"hourly.${hour}.site.${escapeFieldName(v)}" -> BSONInteger(1))
    
    val incEntry:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v = e.id) yield (s"hourly.${hour}.tags.entry.${v.stringify}" -> BSONInteger(1))
    val incTopics:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v <- e.topics) yield (s"hourly.${hour}.tags.topic.${escapeFieldName(v)}" -> BSONInteger(1))
    val incAdj:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v <- e.adjectives) yield (s"hourly.${hour}.tags.adjective.${escapeFieldName(v)}" -> BSONInteger(1))
    val incNouns:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v <- e.nouns) yield (s"hourly.${hour}.tags.noun.${escapeFieldName(v)}" -> BSONInteger(1))

    val incSFs:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v = e.showFirst) yield (s"hourly.${hour}.settings.showFirst.${v.toString}" -> BSONInteger(1))
    val incProts:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v = e.protect) yield (s"hourly.${hour}.settings.protect.${v.toString}" -> BSONInteger(1))
    val incInNews:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v = e.inNews) yield (s"hourly.${hour}.settings.inNews.${v.toString}" -> BSONInteger(1))
    val incInIndex:Seq[(String, BSONInteger)] = for (e <- record.entry.toSeq; v = e.inIndex) yield (s"hourly.${hour}.settings.inIndex.${v.toString}" -> BSONInteger(1))
    
    
    val inc = Seq(incTotal) ++ incUser ++ incSession ++ incTemplate ++ incHow ++ incParams ++ 
    				incEntry ++ incTopics ++ incAdj ++ incNouns ++ incSites ++
    				incSFs ++ incProts ++ incInNews ++ incInIndex
    
    val update = BSONDocument(
      "$inc" -> BSONDocument(inc)
    )
    
    val fle = DB.coll(collectionName).update(query, update, GetLastError(false), upsert=true)
    val fut = fle.map { _ => RefItself("ok") } recover { case x:Throwable => RefFailed(x) }
    new RefFutureRef(fut)

  }
  
  
}