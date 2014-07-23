package com.impressory.api.qna

import com.wbillingsley.handy._
import com.impressory.api._

case class QnAQuestion(

  var text:String,
    
  var answerCount:Int = 0,
  
  var answers:Seq[QnAAnswer] = Seq.empty,
  
  var answerAccepted:Boolean = false

) extends ContentItem {
  
  val itemType = QnAQuestion.itemType
   
}

object QnAQuestion {
  
  val itemType = "Q&A Question"
  
}

case class QnAAnswer(

  id:Id[QnAAnswer,String],
  
  addedBy:Id[User,String],

  val session:Option[String] = None,

  var text:String,
  
  voting:UpDownVoting = new UpDownVoting,
  
  comments:Comments = new Comments,
    
  var accepted:Boolean = false,
  
  val created:Long = System.currentTimeMillis,
  
  var updated:Long = System.currentTimeMillis

) extends HasStringId[QnAAnswer]