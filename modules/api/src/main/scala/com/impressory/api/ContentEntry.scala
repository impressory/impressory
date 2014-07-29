package com.impressory.api

import com.wbillingsley.handy._

case class ContentEntry (
    
  id:Id[ContentEntry, String],
    
  course: Id[Course, String],
  
  addedBy: Id[User, String],

  responseTo: Option[Id[ContentEntry, String]] = None,
  
  item: Option[ContentItem] = None,
  
  tags: CETags = new CETags,
  
  message: CEMessage = new CEMessage,
  
  settings: CESettings = new CESettings,
  
  voting: UpDownVoting = new UpDownVoting,
  
  comments:Comments = new Comments,

  responses:CEResponses = new CEResponses(),

  updated: Long = System.currentTimeMillis,

  created: Long = System.currentTimeMillis
    
) extends HasStringId[ContentEntry] {
  
  def kind = item.map(_.itemType)
  
  /**
   * Two entries are equal if they have the same ID
   */
  override def equals(obj: Any) = {
    obj.isInstanceOf[ContentEntry] &&
      obj.asInstanceOf[ContentEntry].id == id
  }
  
}


case class CEMessage(
  title:Option[String] = None,

  note:Option[String] = None
)

case class CESettings(
  
  showFirst: Boolean = false,
  
  protect: Boolean = false,
  
  inTrash: Boolean = false,
  
  inNews: Boolean = true,
  
  inIndex: Boolean = true,

  allowResponses: Boolean = false,

  published: Option[Long] = None
)

case class CETags(
  adjectives: Set[String] = Set.empty,
  
  nouns: Set[String] = Set.empty,
  
  topics: Set[String] = Set.empty,
  
  site: Option[String] = None    
)

case class CEResponses(

  count: Int = 0,

  entries: Ids[ContentEntry, String] = new Ids(Seq.empty)
)

trait ContentItem { 
  
  val itemType:String
  
  /** 
   *  Whether or not the content can be embedded on third party sites.
   */
  def embeddable:Boolean = true
  
}