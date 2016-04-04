package commons.ws

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.ws.{WSAuthScheme, WSClient, WSProxyServer, WSRequestHolder}
import play.api.{Application, Logger}
import play.modules.statsd.api.StatsdClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class DrunkrApiCredentials(user: String, password: String)

object DrunkrApi {
  type Headers = Seq[DrunkrApiHeaders.Header[_]]
  type ApiResponse[Err,Resp] = Future[Either[Seq[Err],Resp]]

  private def logRequest(wsRequest: WSRequestHolder, action: String, body: String = "")(implicit app: Application) =if(app.configuration.getBoolean("env.reporting.commons.logging").getOrElse(false)){
    Logger.debug(s"$action to ${wsRequest.url} \nquery params: ${wsRequest.queryString} \nheaders: ${wsRequest.headers} \nbody: ${body}")
  }

  class Post[Err,Req,Resp](endpoint: String,
                           path: String,
                           client: WSClient,
                           credentials: DrunkrApiCredentials,
                           headers: DrunkrApi.Headers,
                           statsdClient: Option[StatsdClient] = None,
                           proxy: Option[WSProxyServer] = None) {
     def apply(query: Seq[(String,String)], body: Req)(implicit ec: ExecutionContext, app: Application, rh: ResponseHandler[Err,Resp], writes: Writes[Req]): ApiResponse[Err,Resp] = {
       val wsRequest =
         client.url(s"$endpoint$path")
           .withQueryString(query:_*)
           .withAuth(credentials.user, credentials.password, WSAuthScheme.BASIC)
           .withHeaders(DrunkrApiHeaders.toHeaderSeq(headers): _*)

       val jsBody = Json.toJson(body).toString()
       logRequest(wsRequest, "POST", Json.toJson(body).toString())

       val responseFuture = proxy match {
         case Some(prx) =>  wsRequest.withProxyServer(prx).post(jsBody)
         case None => wsRequest.post(jsBody)
       }

      if(statsdClient.isDefined) {
        val start = System.currentTimeMillis()
        responseFuture.onComplete {
          case Success(r) =>
            val end = System.currentTimeMillis()
            val duration = end - start
            statsdClient.get.timing(s"ws.$path", duration)
            statsdClient.get.increment(s"ws.$path.success", 1)
          case Failure(f) =>
            val end = System.currentTimeMillis()
            val duration = end - start
            statsdClient.get.timing(s"ws.$path", duration)
            statsdClient.get.increment(s"ws.$path.failure", 1)
            Left(Seq.empty)
        }
      }
      responseFuture.map(rh.apply)
    }
  }

  class Put[Err,Req,Resp](endpoint: String,
                          path: String,
                          client: WSClient,
                          credentials: DrunkrApiCredentials,
                          headers: DrunkrApi.Headers,
                          statsdClient: Option[StatsdClient] = None,
                          proxy: Option[WSProxyServer] = None) {
    def apply(query: Seq[(String,String)], body: Req)(implicit ec: ExecutionContext, app: Application, rh: ResponseHandler[Err,Resp], writes: Writes[Req]): ApiResponse[Err,Resp] = {
      val wsRequest =
        client.url(s"$endpoint$path")
          .withQueryString(query:_*)
          .withAuth(credentials.user, credentials.password, WSAuthScheme.BASIC)
          .withHeaders(DrunkrApiHeaders.toHeaderSeq(headers): _*)

      val jsBody = Json.toJson(body).toString()
      logRequest(wsRequest, "PUT", jsBody)

      val responseFuture = proxy match {
        case Some(prx) =>  wsRequest.withProxyServer(prx).put(jsBody)
        case None => wsRequest.put(jsBody)
      }

      if(statsdClient.isDefined) {
        val start = System.currentTimeMillis()
        responseFuture.onComplete {
          case Success(r) =>
            val end = System.currentTimeMillis()
            val duration = end - start
            statsdClient.get.timing(s"ws.$path", duration)
            statsdClient.get.increment(s"ws.$path.success", 1)
          case Failure(f) =>
            val end = System.currentTimeMillis()
            val duration = end - start
            statsdClient.get.timing(s"ws.$path", duration)
            statsdClient.get.increment(s"ws.$path.failure", 1)
            Left(Seq.empty)
        }
      }
      responseFuture.map(rh.apply)
    }
  }

  class Get[Err,Resp](endpoint: String,
                      path: String,
                      client: WSClient,
                      credentials: DrunkrApiCredentials,
                      headers: DrunkrApi.Headers,
                      statsdClient: Option[StatsdClient] = None,
                      proxy: Option[WSProxyServer] = None) {
    def apply(query: Seq[(String,String)])(implicit ec: ExecutionContext, app: Application, rh: ResponseHandler[Err,Resp]): ApiResponse[Err,Resp] = {
      val wsRequest =
        client.url(s"$endpoint$path")
          .withQueryString(query:_*)
          .withAuth(credentials.user, credentials.password, WSAuthScheme.BASIC)
          .withHeaders(DrunkrApiHeaders.toHeaderSeq(headers): _*)

      logRequest(wsRequest, "GET")

      val responseFuture = proxy match {
        case Some(prx) =>  wsRequest.withProxyServer(prx).get()
        case None => wsRequest.get()
      }

      if(statsdClient.isDefined) {
        val start = System.currentTimeMillis()
        responseFuture.onComplete {
          case Success(r) =>
            val end = System.currentTimeMillis()
            val duration = end - start
            statsdClient.get.timing(s"ws.$path", duration)
            statsdClient.get.increment(s"ws.$path.success", 1)
          case Failure(f) =>
            val end = System.currentTimeMillis()
            val duration = end - start
            statsdClient.get.timing(s"ws.$path", duration)
            statsdClient.get.increment(s"ws.$path.failure", 1)
            Left(Seq.empty)
        }
      }
      responseFuture.map(rh.apply)
    }
  }

  /**
    * Custom joda DateTime Json Format for the drunkr API spec
    */
  object JodaDateTimeFormat extends Format[DateTime] {
    override def writes(o: DateTime): JsValue = JsString(o.toString())

    override def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsString(s) =>
        try {
          JsSuccess(DateTime.parse(s))
        } catch {
          case e: Exception =>
            JsError(s"Unable to parse $s as DateTime: ${e.getMessage}")
        }
      case _ =>
        JsError(s"Unable to parse ${json.toString} as DateTime")
    }
  }

}
