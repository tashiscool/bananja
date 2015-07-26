package controllers

import models.dao.sapi.{AbstractModel, Survey, SurveyReactiveImpl, UserDaoReactiveImpl}
import play.api.Play
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import services.SurveyServices

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.dao.sapi.Implicits._

/**
 * Created by tashdid.khan on 7/10/2015.
 */
object ResultsAPI extends Controller {

}
