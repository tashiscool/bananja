package bananja.drunkr.models.dao

package models.dao.sapi

import bananja.drunkr.models.User
import reactivemongo.api.{ReadPreference, DefaultDB}

import play.modules.reactivemongo.json.collection.JSONCollection

import java.text.SimpleDateFormat
import java.util
import java.util.{Date}

import com.mongodb.util.{JSONParseException, JSON}
import com.mongodb.{MongoClientURI, MongoClient, DBObject}
import org.joda.time.format.ISODateTimeFormat
import org.springframework.data.mapping.model.MappingException
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.{SimpleMongoDbFactory, MongoOperations, MongoTemplate}
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

import scala.collection.JavaConversions
import scala.collection.JavaConverters._
import scala.concurrent.Future

import play.Logger
//import play.api.Play.current
import play.api.libs.json._

import org.apache.commons.lang3.StringUtils
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.SerializationUtils._
import org.springframework.data.mongodb.core.query.{Criteria, Query}
import reactivemongo.core.commands._
import commons.utils.db.MongoJson
import scala.concurrent.ExecutionContext.Implicits.global
import play.api._
import com.mongodb._
import User._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.ImplicitBSONHandlers._

trait UserDaoReactive extends Serializable{
  def getUserById(principalId: String): Future[Option[User]]
  def createUser(user: User): Future[reactivemongo.api.commands.WriteResult]
  def getUserByQ(name: String): Future[Option[User]]
}
class UserDaoReactiveImpl(db: Future[DefaultDB]) extends UserDaoReactive{
//  val conf = Play.configuration
  val DB_NAME = "db"
  val userCollectionNameString: String = "user"
  object userDao {
    protected var mongoOperations: MongoOperations = null
    protected var converter: MappingMongoConverter = null
    val otherUri = System.getProperties().getProperty("mongodb.uri")
    val URL = otherUri
    val mongo = new MongoClient(new MongoClientURI(URL))
    val dbFactory: MongoDbFactory = new SimpleMongoDbFactory(new MongoURI(URL))
    def fromInstanceDBObject[T](dbo: DBObject, entityClass: Class[T]): T = {
      val context: MongoMappingContext = new MongoMappingContext
      converter = new MappingMongoConverter(dbFactory, context)
      converter.setMapKeyDotReplacement("-")
      mongoOperations = new MongoTemplate(dbFactory, converter)
      val source: T = converter.read(entityClass, dbo)
      return source
    }
    def getInstanceDBObject[T](objectToSave: T): DBObject = {
      val context: MongoMappingContext = new MongoMappingContext
      converter = new MappingMongoConverter(dbFactory, context)
      converter.setMapKeyDotReplacement("-")
      mongoOperations = new MongoTemplate(dbFactory, converter)
      if (!(objectToSave.isInstanceOf[String])) {
        val dbDoc: DBObject = new BasicDBObject
        converter.write(objectToSave, dbDoc)
        return dbDoc
      }
      else {
        try {
          return JSON.parse(objectToSave.asInstanceOf[String]).asInstanceOf[DBObject]
        }
        catch {
          case e: JSONParseException => {
            throw new MappingException("Could not parse given String to save into a JSON document!", e)
          }
        }
      }
    }
  }
  def collection = db.map(_.collection[JSONCollection](userCollectionNameString))
  def getSurveyByQueryString(queryString: Map[String, Seq[String]]): Future[List[User]] = {
    val query: Query = new Query
    queryString.map {case (k,v) =>
      val criteria: Criteria = Criteria.where(k).in(v)
      query.addCriteria(criteria)
    }
    val parsedObject = Json.parse(serializeToJsonSafely(query.getQueryObject)).as[JsObject]
    collection.flatMap(_.find(parsedObject).cursor[JsValue](readPreference = ReadPreference.nearest).collect[List]()).map {
      result =>
        result.map(Survey => userDao.fromInstanceDBObject(MongoJson.fromJson(Survey), classOf[User]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching Survey with name $query from mongo", e)
        List.empty[User]
    }
  }
  override def getUserById(principalId: String): Future[Option[User]] =
    getSurveyByQueryString(Map("id"-> Seq(principalId))).map(_.headOption)

  override def createUser(user: User): Future[reactivemongo.api.commands.WriteResult] = collection.flatMap(_.save(user))
  override def getUserByQ(name: String): Future[Option[User]] =
    getSurveyByQueryString(Map("name" -> Seq(name))).map(_.headOption)
}
