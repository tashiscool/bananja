package bananja.drunkr.models.dao

import bananja.drunkr.models.User
import slick.backend.DatabaseConfig
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by tashdidkhan on 3/15/16.
  */
trait UserDao {
  def getStudentById(userId: String) :Future[List[User]]
  def insert(UserDao: User):Future[Boolean]
  val query:TableQuery[UserTable] = TableQuery[UserTable]
}
class UserDaoImpl(dbConfig: DatabaseConfig[JdbcProfile]) extends UserDao {

  import dbConfig.driver.api._
  val db = dbConfig.db



  override def getStudentById(userId: String): Future[List[User]] ={
    //TODO: figure out if this is right way to catch and fail exceptions -tk
//    val sqlQuery = sql"""SELECT  student.* FROM user_activities_fact_table student WHERE student.user_key = ${userId}""".as[User]
    val sqlQuery = query.filter(_.id === userId )
    db.run(sqlQuery.result).map(_.toList)
  }

  override def insert(user: User): Future[Boolean] = db.run(  query += (user) ).map(_ => true).recover{case e =>
    Logger.debug(s"some $e")
    false
  }
}
