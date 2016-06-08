package bananja.drunkr.modules


import bananja.drunkr.models.dao.models.dao.sapi.{UserDaoReactive, UserDaoReactiveImpl}
import reactivemongo.api.{MongoDriver, MongoConnection, DefaultDB}

import com.typesafe.config.ConfigFactory

import java.net.URI
import commons.cache.{DB, DBClient}
import commons.ws.soap.StatsdSoapHttpCall
import bananja.drunkr.models.dao.{UserDaoImpl, UserDao}
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

import scala.concurrent.Future
import scala.util.Success


//trait ControllerModule extends ServiceModule with DaoModules with DatabaseModule   {
trait ControllerModule {

  import com.softwaremill.macwire._
  //service dependencies
  implicit def ctxBuilder:ServiceContextBuilder
  def userService: UserService

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
  def userDao: UserDaoReactive

  lazy val underlyingHttpCall = new StatsdSoapHttpCall(Statsd)
  lazy val wsClient = WS.client


  //services
  lazy val userService: UserService = wire[UserServiceImpl]
  lazy val authenticationService: AuthenticationService = wire[AuthenticationServiceImpl]
}

//trait DaoModules extends DatabaseModule{
trait DaoModules{
  import com.softwaremill.macwire._
//  def dbConfig: DatabaseConfig[JdbcProfile]

  def db: Future[DefaultDB]
  lazy val userDao: UserDaoReactive = wire[UserDaoReactiveImpl]
}

trait DatabaseModule extends SlickComponents {

//  import com.softwaremill.macwire._
//  lazy val dbConfig = api.dbConfig[JdbcProfile](DbName("default"))

  import com.softwaremill.macwire._

  lazy val config = ConfigFactory.load

  lazy val driver = new MongoDriver

  lazy val db: Future[DefaultDB] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val parsedUri = MongoConnection.parseURI(config getString "mongodb.uri")
    parsedUri match {
      case Success(uri)=>{
        val con = driver.connection(uri)
        con.database(uri.db.get)
      }
      case _ => throw new RuntimeException(s"Could not parse URI '${config getString "mongodb.uri"}'")
    }
  }
}
