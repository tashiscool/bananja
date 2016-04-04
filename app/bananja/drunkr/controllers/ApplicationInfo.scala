package bananja.drunkr.controllers

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import com.typesafe.scalalogging.LazyLogging
import play.api.Play
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

case class EnvironmentInfo(appVersion: String, build: BuildInfo)
case class BuildInfo(id: String, commitHash: String)


class ApplicationInfo(dbConfig: DatabaseConfig[JdbcProfile]) extends Controller with LazyLogging{

  def index = Action {
      Ok(bananja.drunkr.views.html.index("Your new application is ready."))
  }

  def ping = Action {
    Ok("OK")
  }

  def info =  Action.async {
    implicit request =>
      implicit val buildWrites = Json.writes[BuildInfo]
      implicit val envWrites = Json.writes[EnvironmentInfo]

      val env = EnvironmentInfo(
        appVersion = Play.current.configuration.getString("build.app.version").getOrElse("Undefined"),
        build = BuildInfo(
          id = Play.current.configuration.getString("build.id").getOrElse("Undefined"),
          commitHash = Play.current.configuration.getString("build.commit").getOrElse("Undefined")
        )
      )

      Future.successful(Ok(Json.toJson(env)).as("application/json"))
  }
}
