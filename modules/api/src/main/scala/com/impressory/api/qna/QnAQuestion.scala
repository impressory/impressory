package com.impressory.api.qna

import com.wbillingsley.handy.{Ref, RefNone, HasStringId}
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

  id:String,
  
  addedBy:Ref[User] = RefNone,

  val session:Option[String] = None,

  var text:String,
  
  voting:UpDownVoting = new UpDownVoting,
  
  var comments:Seq[EmbeddedComment] = Seq.empty,
    
  var accepted:Boolean = false,
  
  val created:Long = System.currentTimeMillis,
  
  var updated:Long = System.currentTimeMillis

) extends HasStringId