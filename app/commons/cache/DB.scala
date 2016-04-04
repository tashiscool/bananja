package commons.cache

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend._

object DB {
  def apply(config: Config)(implicit ec: ExecutionContext): DBClient = {
    val gfs = _initClient(config)
    new DBClient(gfs)
  }
  private def _initClient(config: Config) = {
    val conf = config
    val connectionString = conf.getString("database.connection-string")
    val username = conf.getString("database.username")
    val password = conf.getString("database.password")

    val db = Database.forURL(connectionString, user = username, password = password, driver = "org.postgresql.Driver")
    db
  }

}
case class DBClient(jdbcBackend: JdbcBackend.DatabaseDef)

