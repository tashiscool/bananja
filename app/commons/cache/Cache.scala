package commons.cache

import akka.util.ByteString

import java.io.{StringWriter, ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream, ObjectStreamClass}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

import play.api.{Application, Logger}

import net.spy.memcached.auth.{AuthDescriptor, PlainCallbackHandler}
import net.spy.memcached.compat.log.{AbstractLogger, Level}
import net.spy.memcached.internal._
import net.spy.memcached.transcoders.Transcoder
import net.spy.memcached.{AddrUtil, CachedData, ConnectionFactoryBuilder, MemcachedClient}
import redis.{ByteStringFormatter, RedisClient}

case class CacheConfig(namespace: String, elastiCacheEnpoint: Option[String] = None
                       , host: Option[String] = None, user: Option[String], password: Option[String], port: Option[Int])

trait CacheAPI {
  def configs(implicit app: Application): CacheConfig = {
    val ns = app.configuration.getString("env.cache.namespace").getOrElse("global")
    val ecEndpoint = app.configuration.getString("env.cache.elasticache.config.endpoint")
    val host = app.configuration.getString("env.cache.host")
    val user = app.configuration.getString("env.cache.user")
    val pwd = app.configuration.getString("env.cache.password")
    val port = app.configuration.getInt("env.cache.port")
    CacheConfig(ns, ecEndpoint, host, user, pwd, port)
  }

  def set[A](key: String, value: A, expiration: Int = 3600)(implicit ec: ExecutionContext): Future[Boolean]
  def get[A](key: String)(implicit ec: ExecutionContext): Future[Option[A]]
  def remove(key: String)(implicit ec: ExecutionContext): Future[Boolean]
}

class ReddisCache(implicit app: Application) extends CacheAPI{
  implicit val system = app.actorSystem

  lazy val client = {
    val host = configs.host.getOrElse(throw new IllegalArgumentException("host is required"))
    val port = configs.port.getOrElse(throw new IllegalArgumentException("port is required"))
    RedisClient(host,port,configs.password,None,configs.namespace)
  }

  override def set[A](key: String, value: A, expiration: Int)(implicit ec: ExecutionContext): Future[Boolean] = {
    val converter = new UniversalConverter[A]
    client.set(key,value,Some(expiration.toLong),None,false,false)(converter.byteStringFormatter)
  }

  override def get[A](key: String)(implicit ec: ExecutionContext): Future[Option[A]] = {
    val converter = new UniversalConverter[A]
    client.get[A](key)(converter.byteStringFormatter)
  }

  override def remove(key: String)(implicit ec: ExecutionContext): Future[Boolean] = client.del(key).map(_ >= 1)
}



class MemcacheCache(implicit app: Application) extends CacheAPI {
  import Implicits._

  lazy val client = {
    System.setProperty("net.spy.log.LoggerImpl", "services.sapi.Slf4JLogger")

    configs.elastiCacheEnpoint.map { endpoint =>
      new MemcachedClient(AddrUtil.getAddresses(endpoint))
    }.getOrElse {
      val addrs = configs.host.map(AddrUtil.getAddresses)
        .getOrElse(throw new RuntimeException("Bad configuration for memcached: missing host(s)"))

      configs.user.map { memcacheUser =>
        val memcachePassword = configs.password.getOrElse {
          throw new RuntimeException("Bad configuration for memcached: missing password")
        }

        // Use plain SASL to connect to memcached
        val ad = new AuthDescriptor(
          Array("PLAIN"),
          new PlainCallbackHandler(memcacheUser, memcachePassword)
        )
        val cf = new ConnectionFactoryBuilder()
          .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
          .setAuthDescriptor(ad)
          .build()

        new MemcachedClient(cf, addrs)
      }.getOrElse {
        new MemcachedClient(addrs)
      }
    }
  }

  val namespace = configs.namespace

  def set[A](key: String, value: A, expiration: Int = 3600)(implicit ec: ExecutionContext): Future[Boolean] = {
    Logger.debug(s"Writing object to cache with key $namespace/$key")
    val f = client.set(s"$namespace/$key", expiration, value, new UniversalTranscoder[A]).map {
      case Some(returned) => returned.booleanValue()
      case None => false
    } recover {
      case t: Throwable =>
        Logger.error(s"Error writing cache object with key $key: ", t)
        false
    }

    f.onComplete {
      case Success(b) if b =>
        Logger.debug(s"Cache set($key) success")
      case Success(b) if !b =>
        Logger.error(s"Cache set($key) failed")
      case Failure(e) =>
        Logger.error(s"Cache set($key) failed", e)
    }

    f
  }

  def get[A](key: String)(implicit ec: ExecutionContext): Future[Option[A]] = {
    Logger.debug(s"Fetching object from cache with key $namespace$key")
    client.asyncGet(s"$namespace/$key", new UniversalTranscoder[A])
  }

  def remove(key: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    Logger.debug(s"Removing object from cache with key $namespace$key")
    val f = client.delete(s"$namespace/$key").map {
      case Some(returned) => returned.booleanValue()
      case None => false
    } recover {
      case t: Throwable =>
        Logger.error(s"Error removing cache object with key $key: ", t)
        false
    }

    f.onComplete {
      case Success(b) if b =>
        Logger.debug(s"Cache set($key) success")
      case Success(b) if !b =>
        Logger.error(s"Cache set($key) failed")
      case Failure(e) =>
        Logger.error(s"Cache set($key) failed", e)
    }

    f
  }

  object Implicits {
    implicit def operationFutureAsScala[T](underlying: OperationFuture[T]): Future[Option[T]] = {
      val p = Promise[Option[T]]()
      underlying.addListener(new OperationCompletionListener {
        def onComplete(f: OperationFuture[_]) {
          val status = f.getStatus //f is underlying
          if (status.isSuccess)
            p success Option(underlying.get)
          else if (status.getMessage == "NOT_FOUND")
            p success None
          else
            p failure new RuntimeException(s"Cache error: ${status.getStatusCode.toString}")

        }
      })
      p.future
    }

    implicit def getFutureAsScala[T](underlying: GetFuture[T]): Future[Option[T]] = {
      val p = Promise[Option[T]]()
      underlying.addListener(new GetCompletionListener {
        def onComplete(f: GetFuture[_]) {
          val status = f.getStatus //f is underlying
          if (status.isSuccess)
            p success Option(underlying.get)
          else if (status.getMessage == "NOT_FOUND")
            p success None
          else
            p failure new RuntimeException(s"Cache error: ${status.getStatusCode.toString}")
        }
      })
      p.future
    }

    implicit def bulkGetFutureAsScala[T](underlying: BulkGetFuture[T]): Future[Map[String, T]] = {
      val p = Promise[Map[String, T]]()
      underlying.addListener(new BulkGetCompletionListener {
        def onComplete(f: BulkGetFuture[_]) {
          val status = f.getStatus //f is underlying
          if (status.isSuccess)
            p success underlying.get.asScala.toMap //java.util.Map -> mutable.Map -> immutable.Map
          else
            p failure new RuntimeException(s"Cache error: ${status.getStatusCode.toString}")
        }
      })
      p.future
    }
  }

}

class Slf4JLogger(name: String) extends AbstractLogger(name) {

  val logger = Logger("portalcache")

  def isDebugEnabled = logger.isDebugEnabled

  def isInfoEnabled = logger.isInfoEnabled

  def log(level: Level, msg: AnyRef, throwable: Throwable) {
    val message = msg.toString
    level match {
      case Level.DEBUG => logger.debug(message, throwable)
      case Level.INFO => logger.info(message, throwable)
      case Level.WARN => logger.warn(message, throwable)
      case Level.ERROR => logger.error(message, throwable)
      case Level.FATAL => logger.error("[FATAL] " + message, throwable)
      case _ => logger.trace(message, throwable)
    }
  }

  override def isTraceEnabled: Boolean = logger.isTraceEnabled
}

class UniversalTranscoder[T] extends Transcoder[T] {
  override def asyncDecode(cachedData: CachedData): Boolean = true

  override def encode(t: T): CachedData = {
    val bos: ByteArrayOutputStream = new ByteArrayOutputStream()
    new ObjectOutputStream(bos).writeObject(t)
    new CachedData(0, bos.toByteArray, CachedData.MAX_SIZE)
  }

  override def getMaxSize: Int = CachedData.MAX_SIZE

  override def decode(cachedData: CachedData): T = {
    new ObjectInputStream(new ByteArrayInputStream(cachedData.getData)) {
      override protected def resolveClass(desc: ObjectStreamClass) = {
        Class.forName(desc.getName(), false, play.api.Play.current.classloader)
      }
    }.readObject().asInstanceOf[T]
  }
}

class UniversalConverter[T] {
  implicit val byteStringFormatter = new ByteStringFormatter[T] {
    def serialize(data: T): ByteString = {
      val bos: ByteArrayOutputStream = new ByteArrayOutputStream()
      new ObjectOutputStream(bos).writeObject(data)
      ByteString( bos.toByteArray )
    }

    def deserialize(bs: ByteString): T = {
      new ObjectInputStream(new ByteArrayInputStream(bs.toByteBuffer.array())) {
        override protected def resolveClass(desc: ObjectStreamClass) = {
          Class.forName(desc.getName(), false, play.api.Play.current.classloader)
        }
      }.readObject().asInstanceOf[T]
    }
  }
}
