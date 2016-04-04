package commons.ws

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import play.api.Application
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}
import play.modules.statsd.api.StatsdClient

sealed trait ServiceAuth
case class BasicAuth(username: String, password: String) extends ServiceAuth
case object Unauthenticated extends ServiceAuth


/**
  * Trait that represents a web service call
  *
  * @tparam A type of Request
  * @tparam B type of Response
  * @tparam E type of Error
  */
trait WebService[A,B,E] {
  self =>

  def marshallRequest(r: A): String
  def endpoint: String
  def auth: ServiceAuth
  def headers: Seq[(String,String)]
  def client: WSClient

  /**
    * Invoke this web service
    *
    * @param r
    * @param ec
    * @param app
    * @param rh
    * @return
    */
  def call(r: A)(implicit ec: ExecutionContext, app: Application, rh: ResponseHandler[E,B]): Future[Either[Seq[E],B]] = {
    val requestHolder = client.url(endpoint).withHeaders(headers:_*)
    auth match {
      case BasicAuth(u,p) =>
        requestHolder.withAuth(u, p, WSAuthScheme.BASIC).post(marshallRequest(r)).map(rh.apply)
      case Unauthenticated =>
        requestHolder.post(marshallRequest(r)).map(rh.apply)
    }
  }

  /**
    * Invoke this web service and gather metrics in Statsd
    *
    * @param r
    * @param ec
    * @param app
    * @param rh
    * @return
    */
  def callWithMetrics(r: A)(implicit ec: ExecutionContext, app: Application, rh: ResponseHandler[E,B], statsd: StatsdClient): Future[Either[Seq[E],B]] = {
    val start = System.currentTimeMillis()
    call(r).andThen {
      case Success(s) =>
        val end = System.currentTimeMillis()
        val duration = end - start
        statsd.timing(s"ws.${self.getClass.getName}", duration)
        statsd.increment(s"ws.${self.getClass.getName}.success", 1)
        s
      case Failure(f) =>
        val end = System.currentTimeMillis()
        val duration = end - start
        statsd.timing(s"ws.${self.getClass.getName}", duration)
        statsd.increment(s"ws.${self.getClass.getName}.failure", 1)
        Left(Seq.empty)
    }
  }
}

/**
  * Typeclass that handles Web Service response. Should return either a sequence of Errors or
  * some response type.
  *
  * @tparam E
  * @tparam B
  */
trait ResponseHandler[E,B] {
  self =>

  def apply(response: WSResponse): Either[Seq[E],B]

  def withTransformation[U](f: B => U): ResponseHandler[E,U] = new ResponseHandler[E,U] {
    override def apply(response: WSResponse): Either[Seq[E], U] = self.apply(response) match {
      case Left(errors) => Left(errors)
      case Right(r) => Right(f(r))
    }
  }
}


