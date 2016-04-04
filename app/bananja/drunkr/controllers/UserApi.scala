package bananja.drunkr.controllers

import bananja.drunkr.models._

import play.api.libs.json.Json


class UserApi()(implicit ctxBuilder: ServiceContextBuilder) extends ServiceController[User] {

  implicit val formatterUser = Json.format[SignInFormData]

  def login(lang: String) = ActionWithContext("",lang) {
    implicit request =>
       request.body.asJson.map(_.as[SignInFormData]) match {
        case Some(resp) => CommandResponse.futureRespond()(SetValue("user", User("123", resp.username,ServiceUser)))
        case None => CommandResponse.futureRespond()(SetValue("user.error", SimpleData("error searching demographics")))
      }
  }
}
