package commons.model

import scala.concurrent.{ExecutionContext, Future}

import play.api.i18n.Lang
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Json._


import com.drunkr.reporting.commons.cache.CacheAPI

object Implicits {

  implicit val langFormat: Format[Lang] = (
    (JsPath \ "language").format[String] and
    (JsPath \ "country").format[String]
    )(Lang.apply, unlift(Lang.unapply))

  implicit class ModelOps[T](model: T) {
    def to[That](implicit mapper: ModelMapper[T,That]): Option[That] = mapper.apply(model)
    def asJson(implicit mapper: ModelMapper[T,JsValue]): Option[JsValue] = mapper.apply(model)
  }

  implicit def playJsonMapper[A](implicit ev: Writes[A]): ModelMapper[A,JsValue] = {
    new ModelMapper[A,JsValue] {
      def apply(from: A): Option[JsValue] = Some(ev.writes(from))
    }
  }

  implicit class ListFormat[A](model: A) {
    def list(implicit format: Format[A]):Format[List[A]]={
      new Format[List[A]] {override def writes(o: List[A]): JsValue = JsArray(o.map(format.writes(_)))

        override def reads(json: JsValue): JsResult[List[A]] = json match{
          case j: JsArray =>{
            val result = j.value.map(format.reads(_).get).toList
            JsSuccess(result)
          }
          case _ => JsError("")
        }
      }
    }
  }

}


trait ModelLike[T] {

  self: T =>

  def uuid: String

  def caching(expiration: Int = 3600)(implicit cache: CacheAPI, ec: ExecutionContext): Future[T] = cache.set(uuid, self, expiration).map {
    case true => self
    case false => throw new RuntimeException(s"Failed to cache")
  }

}

object Models {
  def fromCache[A](uuid: String)(implicit cache: CacheAPI, ec: ExecutionContext): Future[Option[A]] = cache.get(uuid)

  /**
   * Provides a formatter for a JSON object nested beneath a single JSON object key
   * e.g.: { "NodeName": { "RealObjectKey": "RealObjectValue" }}
   *
   * @param nodeName The name of the node that contains the real object data
   * @param delegate The formatter for the real object data
   * @tparam T The concrete data-type for the real object data mapping
   * @return A nested formatter capable of processing JSON for the specified type nested by the specified node name
   */
  def nestedFormatReader[T](nodeName: String)(delegate: Reads[T]): Reads[T] = new Reads[T] {
    override def reads(json: JsValue): JsResult[T] ={
      val f = (json \ nodeName) match {
        case JsDefined(v) => v
        case undefined: JsUndefined => throw new IllegalArgumentException(undefined.validationError.toString)
      }
      delegate.reads(f)
    }
  }

  def nestedFormatWriter[T](nodeName: String)(delegate: Writes[T]): Writes[T] = new Writes[T] {
    override def writes(o: T): JsValue = JsObject(Seq(nodeName -> delegate.writes(o)))
  }
  def nestedFormat[T](nodeName: String)(delegate: Format[T]): Format[T] = new Format[T] {
    override def reads(json: JsValue): JsResult[T] = nestedFormatReader(nodeName)(delegate).reads(json)
    override def writes(o: T): JsValue = nestedFormatWriter(nodeName)(delegate).writes(o)
  }
}

/**
  *
  * Typeclass for mapping model to other type
  *
  * @tparam From
  * @tparam To
  */
trait ModelMapper[From, To] {
  def apply(from: From): Option[To]
}
class JsMapper[From](implicit format: Format[From]) extends ModelMapper[From,JsValue]{
  def apply(from: From) ={
    Some(format.writes(from))
  }
}
