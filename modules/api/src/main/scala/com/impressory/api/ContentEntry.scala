package com.impressory.api

import com.wbillingsley.handy._
import com.wbillingsley.handy.appbase.JsonConverter
import com.wbillingsley.encrypt.Encrypt

case class ContentEntry (
    
  id:String,
    
  course: RefWithId[Course] = RefNone,
  
  addedBy: RefWithId[User] = RefNone,
  
  item: Option[ContentItem] = None,
  
  tags: CETags = CETags(),
  
  title:Option[String] = None,
  
  note:Option[String] = None,
  
  settings: CESettings = CESettings(),
  
  voting: UpDownVoting = new UpDownVoting,
  
  commentCount:Int = 0,

  comments:Seq[EmbeddedComment] = Seq.empty,
    
  updated: Long = System.currentTimeMillis,
  
  var published: Option[Long] = None,

  created: Long = System.currentTimeMillis
    
) extends HasStringId {
  
  def kind = item.map(_.itemType)
  
  def setPublished(p:Boolean) {
    if (p) {
      published = published orElse Some(System.currentTimeMillis())
    } else {
      published = None
    }
  }
  
  /**
   * Two entries are equal if they have the same ID
   */
  override def equals(obj: Any) = {
    obj.isInstanceOf[ContentEntry] &&
      obj.asInstanceOf[ContentEntry].id == id
  }
  
}


case class CESettings(
  
  showFirst: Boolean = false,
  
  protect: Boolean = false,
  
  inTrash: Boolean = false,
  
  inNews: Boolean = true,
  
  inIndex: Boolean = true
)

case class CETags(
  adjectives: Set[String] = Set.empty,
  
  nouns: Set[String] = Set.empty,
  
  topics: Set[String] = Set.empty,
  
  site: Option[String] = None    
)

trait ContentItem { 
  
  val itemType:String
  
  /** 
   *  Whether or not the content can be embedded on third party sites.
   */
  def embeddable:Boolean = true
  
}