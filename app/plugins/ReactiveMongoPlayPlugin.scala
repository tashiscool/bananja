/*
 * Copyright 2012 Pascal Voitot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package plugins

import play.api._
import reactivemongo.api._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.libs.concurrent.Akka
import reactivemongo.api._
import reactivemongo.core.commands._
import reactivemongo.core.nodeset.Authenticate
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Failure, Success }
import scala.util.control.NonFatal

class ReactiveMongoPlayPlugin(app: Application) extends Plugin {
  private var _helper: Option[ReactiveMongoPlayHelper] = None
  def helper = _helper.getOrElse(throw new RuntimeException("ReactiveMongoPlayPlugin error: no ReactiveMongoPlayHelper available?"))

  override def onStart {
    Logger info "ReactiveMongoPlayPlugin starting..."
    try {
      val conf = ReactiveMongoPlayPlugin.parseConf(app)
      _helper = Some(ReactiveMongoPlayHelper(conf, app))
      Logger.info("ReactiveMongoPlayPlugin successfully started with db '%s'! Servers:\n\t\t%s"
        .format(
          conf.db.get,
          conf.hosts.map { s => s"[${s._1}:${s._2}]" }.mkString("\n\t\t")))
    } catch {
      case NonFatal(e) =>
        throw new PlayException("ReactiveMongoPlayPlugin Initialization Error", "An exception occurred while initializing the ReactiveMongoPlayPlugin.", e)
    }
  }

  override def onStop {
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    Logger.info("ReactiveMongoPlayPlugin stops, closing connections...")
    _helper.map { h =>
      val f = h.connection.askClose()(10 seconds)
      f.onComplete {
        case e => {
          Logger.info("ReactiveMongo Connections stopped. [" + e + "]")
        }
      }
      Await.ready(f, 10 seconds)
      h.driver.close()
    }
    _helper = None
  }
}

/**
 * MongoDB access methods.
 */
object ReactiveMongoPlayPlugin {
  val DefaultPort = 27017
  val DefaultHost = "localhost:27017"

  import play.modules.reactivemongo.json.collection._

  /** Returns the current instance of the driver. */
  def driver(implicit app: Application) = current.helper.driver
  /** Returns the current MongoConnection instance (the connection pool manager). */
  def connection(implicit app: Application) = current.helper.connection
  /** Returns the default database (as specified in `application.conf`). */
  def db(implicit app: Application) = current.helper.db

  /** Returns the current instance of the plugin. */
  def current(implicit app: Application): ReactiveMongoPlayPlugin = app.plugin[ReactiveMongoPlayPlugin] match {
    case Some(plugin) => plugin
    case _            => throw new PlayException("ReactiveMongoPlayPlugin Error", "The ReactiveMongoPlayPlugin has not been initialized! Please edit your conf/play.plugins file and add the following line: '400:play.modules.reactivemongo.ReactiveMongoPlayPlugin' (400 is an arbitrary priority and may be changed to match your needs).")
  }

  /** Returns the current instance of the plugin (from a [[play.Application]] - Scala's [[play.api.Application]] equivalent for Java). */
  def current(app: play.Application): ReactiveMongoPlayPlugin = app.plugin(classOf[ReactiveMongoPlayPlugin]) match {
    case plugin if plugin != null => plugin
    case _                        => throw new PlayException("ReactiveMongoPlayPlugin Error", "The ReactiveMongoPlayPlugin has not been initialized! Please edit your conf/play.plugins file and add the following line: '400:play.modules.reactivemongo.ReactiveMongoPlayPlugin' (400 is an arbitrary priority and may be changed to match your needs).")
  }

  private def parseLegacy(app: Application): MongoConnection.ParsedURI = {
    val conf = app.configuration
    val otherUri = System.getProperties().getProperty("mongodb.uri")

    val db = if(otherUri != null) otherUri else conf.getString("mongodb.db").getOrElse(throw conf.globalError("Missing configuration key 'mongodb.db'!"))
    val uris = conf.getStringList("mongodb.servers") match {
      case Some(list) => scala.collection.JavaConversions.collectionAsScalaIterable(list).toList
      case None       => List(DefaultHost)
    }
    val nodes = uris.map { uri =>
      uri.split(':').toList match {
        case host :: port :: Nil => host -> {
          try {
            val p = port.toInt
            if (p > 0 && p < 65536)
              p
            else throw conf.globalError(s"Could not parse URI '$uri': invalid port '$port'")
          } catch {
            case _: NumberFormatException => throw conf.globalError(s"Could not parse URI '$uri': invalid port '$port'")
          }
        }
        case host :: Nil => host -> DefaultPort
        case _           => throw conf.globalError(s"Could not parse host '$uri'")
      }
    }

    var opts = MongoConnectionOptions()
    for (nbChannelsPerNode <- conf.getInt("mongodb.options.nbChannelsPerNode"))
      opts = opts.copy(nbChannelsPerNode = nbChannelsPerNode)
    for (authSource <- conf.getString("mongodb.options.authSource"))
      opts = opts.copy(authSource = Some(authSource))
    for (connectTimeoutMS <- conf.getInt("mongodb.options.connectTimeoutMS"))
      opts = opts.copy(connectTimeoutMS = connectTimeoutMS)
    for (tcpNoDelay <- conf.getBoolean("mongodb.options.tcpNoDelay"))
      opts = opts.copy(tcpNoDelay = tcpNoDelay)
    for (keepAlive <- conf.getBoolean("mongodb.options.keepAlive"))
      opts = opts.copy(keepAlive = keepAlive)

    val authenticate = {
      val username = conf.getString("mongodb.credentials.username")
      val password = conf.getString("mongodb.credentials.password")
      if (username.isDefined && !password.isDefined || !username.isDefined && password.isDefined)
        throw conf.globalError("Could not parse credentials: missing username or password")
      else if (username.isDefined && password.isDefined)
        Some(Authenticate.apply(opts.authSource.getOrElse(db), username.get, password.get))
      else None
    }

    MongoConnection.ParsedURI(
      hosts = nodes,
      options = opts,
      ignoredOptions = Nil,
      db = Some(db),
      authenticate = authenticate)
  }

  private def parseConf(app: Application): MongoConnection.ParsedURI = {
    app.configuration.getString("mongodb.uri") match {
      case Some(uri) =>
        MongoConnection.parseURI(uri) match {
          case Success(parsedURI) if parsedURI.db.isDefined =>
            parsedURI
          case Success(_) =>
            throw app.configuration.globalError(s"Missing database name in mongodb.uri '$uri'")
          case Failure(e) => throw app.configuration.globalError(s"Invalid mongodb.uri '$uri'", Some(e))
        }
      case _ =>
        parseLegacy(app)
    }
  }
}

private[plugins] case class ReactiveMongoPlayHelper(parsedURI: MongoConnection.ParsedURI, app: Application) {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  lazy val driver = new MongoDriver(Akka.system(app))
  lazy val connection = driver.connection(parsedURI)
  lazy val db = DB(parsedURI.db.get, connection)
}
