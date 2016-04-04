package commons.mvc

import scala.concurrent.Future
import scala.annotation.meta.param
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.{Status, _}

import com.drunkr.reporting.commons.model.Implicits._
import com.drunkr.reporting.commons.model.{ModelLike, ModelMapper}



sealed trait CommandResponse {
  def command: String
  protected def keyValueJson(key: String, value: JsValue) = Json.obj("key" -> key, "value" -> value)
  protected def makeIt(cmd: String)(data: JsValue): JsValue = Json.obj("cmd" -> cmd, "data" -> data)

}

/**
 * Example:
 *object ProcessData extends CommandWithKeyValue("setValue")
 * case class Foo(s: String)
 * implicit def fooFormat = Json.format[Foo]
 * ProcessData(JsString("foo"))
 * ProcessData(Some(JsString("foo")))
 * ProcessData(None)
 * ProcessData(Foo("my string"))
 */

abstract class CommandWithData(val command: String) extends CommandResponse {
  def apply() = makeIt(command)(JsNull)
  def apply(data: JsValue): JsValue = makeIt(command)(data)
  def apply(data: Option[JsValue]): JsValue = makeIt(command)(data.getOrElse(JsNull))
  def apply[T](value: T)(implicit mapper: ModelMapper[T, JsValue]):JsValue = makeIt(command)(mapper(value).getOrElse(JsNull))
}

/**
 * Example
 * object SetValue extends CommandWithKeyValue("setValue")
 * case class Foo(s: String)
 * implicit def fooFormat = Json.format[Foo]
 * SetValue("keyname", JsString("my string"))
 * SetValue("keyname", Some(JsString("my string"))
 * SetValue("keyname", None)
 * SetValue("keyname", Foo(s))
 */
abstract class CommandWithKeyData(val command: String) extends CommandResponse {
  def apply(key: String, value: JsValue): JsValue = makeIt(command)(keyValueJson(key, value))
  def apply(key: String, value: Option[JsValue]): JsValue = makeIt(command)(keyValueJson(key, value.getOrElse(JsNull)))
  def apply[T](key: String, value: T)(implicit mapper: ModelMapper[T, JsValue]):JsValue = makeIt(command)(keyValueJson(key, mapper(value).getOrElse(JsNull)))
}



  object CommandResponse {
      /**
       * takes multiple Response objects transforms them into a JsArray and creates a Result
       * @param responses
       * @return a Result
       */
      def respond(status: Status = Ok)(responses: JsValue*): Result = status(JsArray(responses)).as("application/json")

      /**
       * wraps a response call in a Future.successful
       * @param responses
       * @return a Future[Result]
       */
      def futureRespond(status: Status = Ok)(responses: JsValue*): Future[Result] = Future.successful(respond(status)(responses: _*))


  }




