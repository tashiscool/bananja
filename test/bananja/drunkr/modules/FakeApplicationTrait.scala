package bananja.drunkr.modules

import play.api.test.FakeApplication

/**
  * Created by tashdidkhan on 3/18/16.
  */
trait FakeApplicationTrait {
  implicit val fakeApp = FakeApplication(additionalConfiguration = Map(
    "auth.max.idle.milliseconds" -> 1000L,
    "auth.remember.duration" -> 10,
    "application.secret" -> "@i31ipds@]1uq993XS0KugfY6VX_Ox_u2QgTq/O<sK0oief4vh6<E`=TWhQKA<7B",
    "cache.host"->"localhost:11211",
    "cache.namespace"->"drunkr"
  ))
}
