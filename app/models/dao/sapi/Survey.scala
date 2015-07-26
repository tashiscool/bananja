package models.dao.sapi

import org.apache.commons.lang3.StringUtils
import play.api.libs.json._

/**
 * Created by tashdid.khan on 7/10/2015.
 */
case class Survey(id: String = null, lineItems: Map[Category, List[LineItem]] = Map[Category, List[LineItem]](),  name :String = StringUtils.EMPTY, sourceUser: User = null, targetUser: User = null)
case class LineItem (id: String = null, minRange: Int =0,maxRange: Int =0, question: String, score: Int = 0)
case class Category(categoryname: String)

case class Skill(id: String, name :String, profency: Int)
case class ExternalSource(id: String, name: String, source: String)
case class Award(id :String, ranking :Int, participantCount: String)

case class Education(id: String, schoolName :String, yearStarted: Int, yearEnded: Int, completed: Boolean, level: Int, skillslearned: List[Skill], sourcesReferenced: List[ExternalSource], awardswon: List[Award] )
case class Experience(id: String, companyName: String, yearStarted: Int, yearEnded: Int, title: String, present: Boolean,
                       skillsUsed: List[Skill], sourcesReferenced: List[ExternalSource], awardswon: List[Award])


object Implicits {
  
}





