package bananja.drunkr.modules

import java.net.URI
import com.bananja.reporting.commons.cache.{DB, DBClient}
import com.bananja.reporting.commons.ws.soap.StatsdSoapHttpCall
import controllers.{ServiceContextBuilder}
import bananja.drunkr.models.dao.{StudentGradeDaoImpl, StudentGradeDao}
import bananja.drunkr.services._
import play.api.{BuiltInComponents, Play}
import play.api.libs.ws.WS

import com.softwaremill.macwire._
import bananja.drunkr.controllers._
import controllers.{Assets}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.routing.Router
import router.Routes
import play.api.Play.current
import play.modules.statsd.api.Statsd
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import play.api.db.slick.{DbName, SlickComponents}




//trait ControllerModule extends ServiceModule with DaoModules with DatabaseModule   {
trait ControllerModule {

  import com.softwaremill.macwire._
  //service dependencies
  def dbConfig: DatabaseConfig[JdbcProfile]

  implicit def ctxBuilder:ServiceContextBuilder

  //controllers
  lazy val applicationController = wire[ApplicationInfo]
  lazy val userController = wire[UserApi]

}

trait ContextBuilderModule{
  import com.softwaremill.macwire._
  def userService: UserService
  def authenticationService: AuthenticationService
  implicit val ctxBuilder = wire[ServiceContextBuilder]
}

//trait ServiceModule extends DaoModules with DatabaseModule{
trait ServiceModule {

  import com.softwaremill.macwire._
  //dependencies
  def studentGradeDao: StudentGradeDao

  lazy val underlyingHttpCall = new StatsdSoapHttpCall(Statsd)
  lazy val wsClient = WS.client


  //services
  lazy val userService: UserService = wire[UserServiceImpl]
  lazy val authenticationService: AuthenticationService = wire[AuthenticationServiceImpl]
}

//trait DaoModules extends DatabaseModule{
trait DaoModules{
  import com.softwaremill.macwire._
  def dbConfig: DatabaseConfig[JdbcProfile]
  lazy val studentGradeDao: StudentGradeDao = wire[StudentGradeDaoImpl]
}

trait DatabaseModule extends SlickComponents {

  import com.softwaremill.macwire._
  lazy val dbConfig = api.dbConfig[JdbcProfile](DbName("default"))
}
