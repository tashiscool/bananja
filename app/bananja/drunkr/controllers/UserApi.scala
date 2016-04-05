package bananja.drunkr.controllers

import bananja.drunkr.models._

import commons.mvc.{UserRole, SetValue, ServiceController, CommandResponse}
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class UserApi()(implicit ctxBuilder: ServiceContextBuilder) extends ServiceController[User] {

  implicit val formatterUser = Json.format[SignInFormData]

  def login(lang: String) = ActionWithContext("",lang) {
    implicit request =>
       request.body.asJson.map(_.as[SignInFormData]) match {
        case Some(resp) => CommandResponse.futureRespond()(SetValue("user", User("123", resp.username,UserRole.admin)))
        case None => CommandResponse.futureRespond()(SetValue("user.error", SimpleData("error searching demographics")))
      }
  }
}