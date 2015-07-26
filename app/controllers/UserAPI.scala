package controllers

import models.dao.sapi.{AbstractModel, User, UserDaoReactiveImpl}
import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by tashdid.khan on 7/10/2015.
 */
object UserAPI extends Controller {
  val client = UserDaoReactiveImpl


  implicit val implicitBarWrites = new Writes[User] {
    def writes(bar: User): JsValue = {
      Json.parse(bar.toJson)
    }
  }
  implicit val implicitBarReads = new Reads[User] {
    def reads(json: JsValue): JsResult[User] = {
      try{
        JsSuccess(AbstractModel.fromJson(Json.stringify(json),classOf[User]).getOrElse(throw new IllegalArgumentException("unable to read user")))
      }catch {
          case e :Throwable => JsError()
        }
    }
  }
  def getUser(id: String) = Action.async {
    implicit request =>
      client.getUserById("",id).map {
        case Some(user) =>
          Ok(Json.toJson(user)).as("application/json")
        case _ => NotFound
      } recover {
        case _ => NotFound
      }

  }

  def createUser() = Action.async {
    implicit request =>
      request.contentType match {
        case Some("application/json") if request.body.asJson.isDefined =>
          client.createUser(Json.fromJson[User](request.body.asJson.get).getOrElse(throw new IllegalArgumentException("UnsupportedMediaType"))).map{ error =>
            if(error.ok)Ok
            else InternalServerError(error.stringify)
          }
        case _ => Future.successful(UnsupportedMediaType)
      }
  }

  def query() = Action.async {
    implicit request =>
      client.getUserByQueryString(request.queryString).map {
        fileList =>
          Ok(Json.toJson(fileList)).as("application/json")
      }

  }

  def updateUser(id: String) = Action.async {
    implicit request =>
      request.contentType match {
        case Some("application/json") if request.body.asJson.isDefined =>
          client.getUserById("",id).flatMap {
            case Some(user) =>
              client.updateUser(Json.fromJson[User](request.body.asJson.get).getOrElse(throw new IllegalArgumentException("UnsupportedMediaType"))).map{ error =>
                if(error.ok)Ok(Json.toJson(user)).as("application/json")
                else InternalServerError(error.stringify)
              }
            case _ => Future.successful(NotFound)
          } recover {
            case _ => NotFound
          }

        case _ => Future.successful(UnsupportedMediaType)
      }
  }

}
