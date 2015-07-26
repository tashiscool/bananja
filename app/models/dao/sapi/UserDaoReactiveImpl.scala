package models.dao.sapi

import java.util
import java.util.UUID

import com.mongodb.util.{JSONParseException, JSON}
import com.mongodb.{MongoClientOptions, MongoClientURI, MongoClient, DBObject}
import org.springframework.data.authentication.UserCredentials
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


trait UserDaoReactive {
  def createUser(User: User): Future[LastError]

  def getUserById(vendor: String, id: String): Future[Option[User]]

  def getUserByName(name: String): Future[Option[User]]

  def getUserByLinkedAccount(id: String, Provider: String): Future[Option[User]]

  def updateUser(User: User): Future[LastError]

  def deleteUser(User: User): Future[LastError]

  def removeUnverifiedUser(name: String): Future[LastError]

  def getUserByUsernameVendor(vendor: String, username: String): Future[Option[User]]

  def findUnverifiedUser(name: String): Future[Option[User]]

}

object UserDaoReactiveImpl {
  val conf = Play.configuration
  val DB_NAME = "db"
  val userCollectionNameString: String = "users"
  object userDao {
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
  def collection = db.collection[JSONCollection](userCollectionNameString)

  def getUserById(vendor: String, id: String): Future[Option[User]] = {
    val query: Query = new Query
    val orDquery: Query = new Query
    val criteriaNoMapping: Criteria = Criteria.where("usernameMapping").exists(false)
    val criteria: Criteria = Criteria.where("userId").is(id).and("usernameMapping").exists(false)
    val subCriteria: Criteria = Criteria.where("vendor").is(vendor).and("userId").is(id)
    val mappingCriteria: Criteria = Criteria.where("usernameMapping").elemMatch(subCriteria)
    query.addCriteria(new Criteria().orOperator(mappingCriteria, criteria))
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
          result =>
            result.map(user => userDao.fromInstanceDBObject(MongoJson.fromJson(user), classOf[User]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error reading searching for user $id in mongo",e)
        None
    }

  }

  def getUserByPartyId(id: String): Future[Option[User]] = {
    val query: Query = new Query
    val criteria: Criteria = Criteria.where("partyId").is(id)
    query.addCriteria(criteria)
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
        result =>
          result.map(user => userDao.fromInstanceDBObject(MongoJson.fromJson(user), classOf[User]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching user with party ID $id from mongo",e)
        None
    }

  }

  def getUserByQueryString(queryString: Map[String, Seq[String]]): Future[List[User]] = {
    val query: Query = new Query
    queryString.map {case (k,v) =>
      val criteria: Criteria = Criteria.where(k).in(v)
      query.addCriteria(criteria)
    }
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).cursor[JsValue].collect[List]().map {
      result =>
        result.map(user => userDao.fromInstanceDBObject(MongoJson.fromJson(user), classOf[User]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching user with name $query from mongo", e)
        List.empty[User]
    }
  }

  def getUserByName(name: String): Future[Option[User]] = {
    val query: Query = new Query
    val criteria: Criteria = Criteria.where("username").is(name)
    query.addCriteria(criteria)
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(user => userDao.fromInstanceDBObject(MongoJson.fromJson(user), classOf[User]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching user with name $name from mongo", e)
        None
    }
  }

  def getUserByNameWithNoMapping(name: String): Future[Option[User]] = {
    val query: Query = new Query
    val criteria: Criteria = Criteria.where("username").is(name)
    val criteriaNoMapping: Criteria = Criteria.where("usernameMapping").exists(false)
    query.addCriteria(criteria).addCriteria(criteriaNoMapping)
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(user => userDao.fromInstanceDBObject(MongoJson.fromJson(user), classOf[User]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching user with no mapping and name $name from mongo", e)
        None
    }
  }

  def createUser(user: User): Future[LastError] = {
    sanitizeForSave(user).flatMap {
      case Some(sanitizedUser) => collection.save(MongoJson.toJson(userDao.getInstanceDBObject(sanitizedUser)))
      case None => collection.save(MongoJson.toJson(userDao.getInstanceDBObject(user)))
    }
  }

  def deleteUser(user: User): Future[LastError] = {
    val query: Query = new Query().addCriteria(Criteria.where("_id").is(user.id))
    collection.remove(Json.parse(serializeToJsonSafely(query.getQueryObject)))
  }

  def removeUnverifiedUser(name: String): Future[LastError] = {
    val criteria: Criteria = Criteria.where("username").is(name)
      .andOperator(new Criteria().orOperator(
        Criteria.where("partyId").regex("^Dummy"),
      new Criteria().orOperator( Criteria.where("userId").regex("^Dummy"),Criteria.where("userId").is(""))))
    val query: Query = new Query().addCriteria(criteria)
    val queryString = Json.parse(serializeToJsonSafely(query.getQueryObject))
    Logger.debug("using query to remove "+serializeToJsonSafely(query.getQueryObject))

    collection.remove(queryString)
  }

  def findUnverifiedUser(name: String): Future[Option[User]] = {
  val criteria: Criteria = Criteria.where("username").is(name)
    .andOperator(new Criteria().orOperator(
    Criteria.where("partyId").regex("^Dummy"),
    new Criteria().orOperator( Criteria.where("userId").regex("^Dummy"),Criteria.where("userId").is(""))))
  val query: Query = new Query().addCriteria(criteria)
  Logger.debug("using query to find "+serializeToJsonSafely(query.getQueryObject))
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(user => userDao.fromInstanceDBObject(MongoJson.fromJson(user), classOf[User]))
    } recover {
      case e: Exception =>
        Logger.error(s"Error fetching user with name $name from mongo", e)
        None
    }
  }

  def updateUser(user: User): Future[LastError] = {
    sanitizeForSave(user).flatMap {
      case Some(sanitizedUser) => collection.save(MongoJson.toJson(userDao.getInstanceDBObject(sanitizedUser)))
      case None => collection.save(MongoJson.toJson(userDao.getInstanceDBObject(user)))
    }
  }

  def getUserByLinkedAccount(id: String, provider: String): Future[Option[User]] = {
    val query: Query = new Query
    val subCriteria: Criteria = Criteria.where("identityProvider").is(provider).and("_id").is(id)
    val criteria: Criteria = Criteria.where("linkedAccounts").elemMatch(subCriteria)
    query.addCriteria(criteria)
    collection.find(Json.parse(serializeToJsonSafely(query.getQueryObject))).one[JsValue].map {
      result =>
        result.map(user => userDao.fromInstanceDBObject(MongoJson.fromJson(user), classOf[User]))
      } recover {
      case e: Exception =>
        Logger.error(s"Error fetching user with linked account $provider/$id from mongo", e)
        None
    }
  }

  def getUserByUsernameVendor(vendor: String, username: String): Future[Option[User]] = {
    val searchId = UUID.randomUUID().toString
    Logger.debug(s"[$searchId] Query for user with vendor $vendor and username $username")
    val query: Query = new Query
    val subCriteria: Criteria = Criteria.where("vendor").is(vendor).and("username").is(username.toLowerCase)
    val criteria: Criteria = Criteria.where("usernameMapping").elemMatch(subCriteria)
    query.addCriteria(criteria)
    val jsonQuery = Json.parse(serializeToJsonSafely(query.getQueryObject))
    collection.find(jsonQuery).cursor[JsValue].collect[List]().flatMap { q =>
      if (q.size < 1) {
        Logger.debug(s"[$searchId] No user found")
        getUserByNameWithNoMapping(username)
      } else if (q.size == 1) {
        val returnUser = userDao.fromInstanceDBObject(MongoJson.fromJson(q.head), classOf[User])
        Logger.debug(s"[$searchId] Found user: ${returnUser.id}")
        Future.successful(Some(returnUser))
      } else {
        val userList = q.map {
          p => userDao.fromInstanceDBObject(MongoJson.fromJson(p), classOf[User])
        }
        Logger.debug(s"[$searchId] Found multiple users: ${userList.map(_.id).toString()}")
        val userFilteredList = userList.filter(!_.partyId.startsWith("Dummy"))
        if (!userFilteredList.isEmpty) {
          Logger.debug(s"[$searchId] Returning non-dummy user: ${userFilteredList.head.id}")
          Future.successful(Some(userFilteredList.head))
        } else {
          Logger.debug(s"[$searchId] All users were dummies. Defaulting to user: ${userList.head.id}")
          Future.successful(Some(userList.head))
        }
      }
    } recover {
      case error: Throwable =>
        Logger.error(s"[$searchId] Mongo had a problem:", error)
        None
    }
  }

  protected def getCollectionName: String = userCollectionNameString


  private def preserveElements(user: User, returneduser: Option[User]): User = {
    returneduser match {
      case Some(r) =>
        user.id = r.id
        if (user.coached || r.coached)
          user.coached = true
        val usernameMappingsToAdd = r.usernameMapping.asScala.filterNot{ u => user.usernameMapping.asScala.exists(_.vendor == u.vendor) }
        user.usernameMapping.addAll(new HashSet(usernameMappingsToAdd.asJava))
        user
      case None =>
        user
    }
  }

  //TODO Mutable variables are bad! Use immutable data here
  private def updateUserNametoLowerCase(user: User): User = {
    user.usernameMapping.asScala.foreach(vendorMapping => vendorMapping.username = vendorMapping.username.toLowerCase)
    user
  }

  private def sanitizeForSave(user: User): Future[Option[User]] = {
    Logger.debug(s"Sanitizing user: ${user.toJson}")
    val default: Option[User] = None

    val userThree: Future[List[Option[User]]] = if (StringUtils.isNotBlank(user.partyId)) {
      Future.sequence {
        List(
          getUserByPartyId(user.partyId).map {
            returneduser =>
              Some(preserveElements(user, returneduser))
          })
      }
    }
    else if (StringUtils.isNotBlank(user.username) && !user.usernameMapping.isEmpty) {
      Future.sequence {
        user.withUsername(user.username.toLowerCase).usernameMapping.asScala.toList.map {
          mapping =>
            getUserByUsernameVendor(mapping.vendor, mapping.username.toLowerCase).map {
              returneduser =>
                Some(preserveElements(user, returneduser))
            }
        }
      }
    }
    else if (StringUtils.isNotBlank(user.username)) {
      user.withUsername(user.username.toLowerCase)
      Future.sequence {
        List(
          getUserByNameWithNoMapping(user.username).map {
            returneduser =>
              Some(preserveElements(user, returneduser))
          })
      }
    }
    else {
      Future.sequence {
        List(Future.successful(default))
      }
    }


    val transformedUsers = userThree.map {
      userList =>
        userList.map {
          case Some(u) =>
            Some(updateUserNametoLowerCase(u))
          case None =>
            None
        }
    }

    transformedUsers.map { userDude =>
      if(userDude.head.isDefined){
        val user2 = userDude.head.get
        if (StringUtils.isBlank(user2.id)) {
          user2.id = new ObjectId().toString.replace("-", "")
        }
        if (StringUtils.isBlank(user2.partyId)) {
          user2.partyId = "Dummy" + UUID.randomUUID.toString
        }
        Some(user2)
      }else{
        Some(user)
      }

    }
  }

}