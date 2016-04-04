package commons.ws.soap

import java.net.URI

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import play.api.libs.ws._
import play.modules.statsd.api.{StatsdClient, Statsd}
import play.api.{Application => PlayApp}
import play.api.Logger

/**
 * Created by tkhan
 */

/**
 * Underlying Soap Call is an underlying HTTP client that the Scalaxb's Bindings: => Soap11ClientsAsync: => HttpClientsAsync requires.
 */
trait UnderlyingSoapHttpCall {
  /**
   *
   * @param app
   * @return
   */
  def requestTimeout(implicit app: PlayApp) = app.configuration.getInt("env.ws.soap.timeout").getOrElse(120000)

  /**
   * Posts in to the given address URI through the WSClient and returns the response body
   * @param client
   * @param in
   * @param address
   * @param headers
   * @param app
   * @param context
   * @return  Web Service response body as a string (should be used by scalaxb to generate the required case class)
   */
  def call(client: WSClient)(in: String, address: java.net.URI, headers: Map[String, String])(implicit  app: PlayApp,  context: ExecutionContext):Future[String] = {
    val uuid = java.util.UUID.randomUUID().toString
    Logger.debug(s"[$uuid] Soap Request: $in with headers $headers")
    client.url(address.toString).withRequestTimeout(requestTimeout).withHeaders(headers.toSeq:_*).post(in).map{ res =>
      Logger.debug(s"[$uuid]  Soap Response: ${res.body} with headers: ${res.allHeaders}")
      res.body
    }
  }
  def apply(client: WSClient)(in: String, address: java.net.URI, headers: Map[String, String])(implicit app: PlayApp, context: ExecutionContext):Future[String]
}


object UnmeteredSoapHttpCall extends UnderlyingSoapHttpCall {
  /**
   * calls  UnderlyingSoapHttpCall.apply
   * @param client
   * @param in
   * @param address
   * @param headers
   * @param app
   * @param context
   * @return Web Service response body as a string (should be used by scalaxb to generate the required case class)
   */
  def apply(client: WSClient)(in: String, address: java.net.URI, headers: Map[String, String])(implicit app: PlayApp, context: ExecutionContext):Future[String] = call(client)(in, address, headers)
}

class StatsdSoapHttpCall(statsd: StatsdClient) extends UnderlyingSoapHttpCall {
  self =>
  /**
   * Calls  UnderlyingSoapHttpCall.apply, then it logs the duration to statsd instance
   * @param client
   * @param in
   * @param address
   * @param headers
   * @param app
   * @param context
   * @return  Web Service response body as a string, after logging outcome to statsd (should be used by scalaxb to generate the required case class)
   */
  def apply(client: WSClient)(in: String, address: java.net.URI, headers: Map[String, String])(implicit app: PlayApp, context: ExecutionContext):Future[String] = {
    val start = System.currentTimeMillis()
    call(client)(in, address, headers).andThen {
      case Success(s) =>
        val end = System.currentTimeMillis()
        val duration = end - start
        statsd.timing(s"ws.billing.${self.getClass.getName}", duration)
        statsd.increment(s"ws.billing.${self.getClass.getName}.success", 1)
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
