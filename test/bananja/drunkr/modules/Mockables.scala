package bananja.drunkr.modules

/**
  * Created by tashdidkhan on 3/17/16.
  */

import bananja.drunkr.controllers.ServiceContextBuilder
import bananja.drunkr.services._
import org.scalamock.specs2.MockContext
import org.specs2.mock.Mockito
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait MockContextBuilderModule extends Mockito{
  def userService: UserService
  def authenticationService: AuthenticationService
//  implicit val ctxBuilder:ServiceContextBuilder = new ServiceContextBuilder(userService, authenticationService)
  implicit val ctxBuilder:ServiceContextBuilder = mock[ServiceContextBuilder]
}

trait MockDaoModule extends Mockito with DaoModules {
}
trait MockServiceModule extends Mockito with ServiceModule{

  override lazy val userService: UserService = mock[UserService]
  override lazy val authenticationService: AuthenticationService = mock[AuthenticationService]
}
trait MockDatabaseModule extends Mockito{

  lazy val dbConfig: DatabaseConfig[JdbcProfile] = mock[DatabaseConfig[JdbcProfile]]
}

