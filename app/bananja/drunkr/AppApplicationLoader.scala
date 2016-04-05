package bananja.drunkr

import _root_.controllers.Assets
import bananja.drunkr.models.dao.UserDao
import bananja.drunkr.models.dao.models.dao.sapi.UserDaoReactive
import bananja.drunkr.modules._
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import com.softwaremill.macwire._
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.api.{Logger, _}

import scala.concurrent.ExecutionContext
import router.Routes


class AppApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {

    // make sure logging is configured
    Logger.configure(context.environment)

    (new BuiltInComponentsFromContext(context) with ApplicationModule ).application
  }
}

trait ApplicationModule extends BuiltInComponents with DatabaseModule with DaoModules
  with ServiceModule with ControllerModule with ContextBuilderModule {
  implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val assets: Assets = wire[Assets]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]

  val seed = wire[Seed]
  seed.run()
}


import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
class Seed(
            val userDao: UserDaoReactive
          ) {

  def run(): Unit = {
  }
}
