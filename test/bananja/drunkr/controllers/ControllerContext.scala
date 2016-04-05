package bananja.drunkr.controllers

/**
  * Created by tashdidkhan on 3/17/16.
  */

import bananja.drunkr.modules._
import org.scalamock.specs2.MockContext
import org.specs2.matcher.MustThrownExpectations
import org.specs2.specification.Scope
import play.api.test.FakeApplication

import scala.concurrent.ExecutionContext

trait ControllerContext
  extends ControllerModule
    with MockContextBuilderModule
    with MockServiceModule
    with MockDaoModule
    with MockDatabaseModule
    with Scope
    with MustThrownExpectations
{
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}

trait TestingContext extends MockContext with MockServiceModule
with MockDaoModule
with MockDatabaseModule
with Scope
with MustThrownExpectations{
  implicit val fakeApp = FakeApplication(additionalConfiguration = Map(
    "auth.max.idle.milliseconds" -> 1000L,
    "auth.remember.duration" -> 10,
    "application.secret" -> "@i31ipds@]1uq993XS0KugfY6VX_Ox_u2QgTq/O<sK0oief4vh6<E`=TWhQKA<7B",
    "cache.host"->"localhost:11211",
    "cache.namespace"->"drunkr"
  ))
}
