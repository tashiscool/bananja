package models.dao.sapi

import java.util
import java.util.UUID

import com.mongodb.util.{JSONParseException, JSON}
import com.mongodb.{MongoClientOptions, MongoClientURI, MongoClient, DBObject}
import org.springframework.data.mapping.model.MappingException
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.{SimpleMongoDbFactory, MongoOperations, MongoTemplate}
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

import scala.collection.JavaConverters._
import scala.concurrent.Future

import play.Logger
import play.api.Play.current
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection

import org.apache.commons.lang3.StringUtils
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.SerializationUtils._
import org.springframework.data.mongodb.core.query.{Criteria, Query}
import plugins.ReactiveMongoPlayPlugin
import reactivemongo.core.commands.LastError
import utils.scalautils.MongoJson
import java.util.HashSet
import scala.concurrent.ExecutionContext.Implicits.global
import play.api._
import com.mongodb._



/**
 * Created by tash on 9/19/14.
 */

object SurveyReactiveImpl {
  val conf = Play.configuration
  val DB_NAME = "db"
  val SurveyCollectionNameString: String = "Surveys"
  object SurveyDao {
    protected var mongoOperations: MongoOperations = null
    protected var converter: MappingMongoConverter = null
    val otherUri = System.getProperties().getProperty("mongodb.uri")
    val URL = if(otherUri != null) otherUri else conf.getString("mongodb.db").getOrElse(throw conf.globalError("Missing configuration key 'mongodb.db'!"))
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
  def driver = ReactiveMongoPlayPlugin.driver
  /** Returns the current MongoConnection instance (the connection pool manager). */
  def connection = ReactiveMongoPlayPlugin.connection
  /** Returns the default database (as specified in `application.conf`). */
  def db = ReactiveMongoPlayPlugin.db
  def collection = db.collection[JSONCollection](SurveyCollectionNameString)

  def getSurveyById(surveyId :String, id: String): Future[Option[Survey]] = {
    val query: Query = new Query
    val orDquery: Query = new Query
    val criteria: Criteria = Criteria.where("targetUser.id").is(id)
    query.addCriteria(criteria).addCriteria(Criteria.where("id").is(surveyId))
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
          result =>
            result.map(Survey => SurveyDao.fromInstanceDBObject(MongoJson.fromJson(Survey), classOf[Survey]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error reading searching for Survey $id in mongo",e)
        None
    }

  }

  def getSurveyByQueryString(queryString: Map[String, Seq[String]]): Future[List[Survey]] = {
    val query: Query = new Query
    queryString.map {case (k,v) =>
      val criteria: Criteria = Criteria.where(k).in(v)
      query.addCriteria(criteria)
    }
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).cursor[JsValue].collect[List]().map {
      result =>
        result.map(Survey => SurveyDao.fromInstanceDBObject(MongoJson.fromJson(Survey), classOf[Survey]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching Survey with name $query from mongo", e)
        List.empty[Survey]
    }
  }

  def getSurveyByName(name: String): Future[Option[Survey]] = {
    val query: Query = new Query
    val criteria: Criteria = Criteria.where("name").is(name)
    query.addCriteria(criteria)
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(Survey => SurveyDao.fromInstanceDBObject(MongoJson.fromJson(Survey), classOf[Survey]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching Survey with name $name from mongo", e)
        None
    }
  }

  def getSurveyByNameWithNoMapping(name: String): Future[Option[Survey]] = {
    val query: Query = new Query
    val criteria: Criteria = Criteria.where("Surveyname").is(name)
    val criteriaNoMapping: Criteria = Criteria.where("SurveynameMapping").exists(false)
    query.addCriteria(criteria).addCriteria(criteriaNoMapping)
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(Survey => SurveyDao.fromInstanceDBObject(MongoJson.fromJson(Survey), classOf[Survey]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching Survey with no mapping and name $name from mongo", e)
        None
    }
  }

  def createSurvey(survey: Survey): Future[LastError] = {
    collection.save(MongoJson.toJson(SurveyDao.getInstanceDBObject(survey)))
  }

  def deleteSurvey(survey: Survey): Future[LastError] = {
    val query: Query = new Query().addCriteria(Criteria.where("_id").is(survey.id))
    collection.remove(Json.parse(serializeToJsonSafely(query.getQueryObject)))
  }

  def removeUnverifiedSurvey(name: String): Future[LastError] = {
    val criteria: Criteria = Criteria.where("Surveyname").is(name)
      .andOperator(new Criteria().orOperator(
        Criteria.where("partyId").regex("^Dummy"),
      new Criteria().orOperator( Criteria.where("SurveyId").regex("^Dummy"),Criteria.where("SurveyId").is(""))))
    val query: Query = new Query().addCriteria(criteria)
    val queryString = Json.parse(serializeToJsonSafely(query.getQueryObject))
    Logger.debug("using query to remove "+serializeToJsonSafely(query.getQueryObject))

    collection.remove(queryString)
  }

  def findUnverifiedSurvey(name: String): Future[Option[Survey]] = {
  val criteria: Criteria = Criteria.where("Surveyname").is(name)
    .andOperator(new Criteria().orOperator(
    Criteria.where("partyId").regex("^Dummy"),
    new Criteria().orOperator( Criteria.where("SurveyId").regex("^Dummy"),Criteria.where("SurveyId").is(""))))
  val query: Query = new Query().addCriteria(criteria)
  Logger.debug("using query to find "+serializeToJsonSafely(query.getQueryObject))
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(Survey => SurveyDao.fromInstanceDBObject(MongoJson.fromJson(Survey), classOf[Survey]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching Survey with name $name from mongo", e)
        None
    }
  }

  def updateSurvey(survey: Survey): Future[LastError] = {
      collection.save(MongoJson.toJson(SurveyDao.getInstanceDBObject(survey)))
  }

  def getSurveyByLinkedAccount(id: String, provider: String): Future[Option[Survey]] = {
    val query: Query = new Query
    val subCriteria: Criteria = Criteria.where("identityProvider").is(provider).and("_id").is(id)
    val criteria: Criteria = Criteria.where("linkedAccounts").elemMatch(subCriteria)
    query.addCriteria(criteria)
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(Survey => SurveyDao.fromInstanceDBObject(MongoJson.fromJson(Survey), classOf[Survey]))
      } recover {
      case e: Exception =>
        Logger.error(s"Error fetching Survey with linked account $provider/$id from mongo", e)
        None
    }
  }



  protected def getCollectionName: String = SurveyCollectionNameString

}