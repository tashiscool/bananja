package commons.utils.db

/**
  * Created by tashdidkhan on 4/4/16.
  */
import java.text.SimpleDateFormat

import com.mongodb._
import org.bson.types._
import org.joda.time.format.ISODateTimeFormat

/**
  * Created by tash on 9/22/14.
  */
/**
  * Play Json lib <=> DBObject converter methods (includes implicits).
  */
object MongoJson {
  import java.util.Date

  import play.api.libs.json._

  import scala.collection.JavaConversions

  /** Serializes the given [[com.mongodb.DBObject]] into a [[play.api.libs.json.JsValue]]. */
  def toJson(dbObject: DBObject) :JsValue = Json.toJson(dbObject)(BSONObjectFormat)

  /** Deserializes the given [[play.api.libs.json.JsValue]] into a [[com.mongodb.DBObject]]. */
  def fromJson(v: JsValue) :DBObject = Json.fromJson[DBObject](v)(BSONObjectFormat).asOpt.getOrElse(null)

  /** Formatter for [[com.mongodb.DBObject]], handling serialization/deserialisation for DBObjects. */
  implicit object BSONObjectFormat extends Format[DBObject] {
    def reads(json: JsValue) :JsResult[DBObject] = JsSuccess(parse(json.asInstanceOf[JsObject]))
    def writes(bson: DBObject) :JsValue = Json.parse(bson.toString)

    private def parse(map: JsObject) :BasicDBObject = new BasicDBObject(
      JavaConversions.mapAsJavaMap(Map() ++ map.fields.map { p =>
        (p._1, p._2 match {
          case v: JsObject => {
            specialMongoJson(v).fold (
              normal => parse(normal),
              special => special
            )
          }
          case v: JsArray => { parse(v) }
          case v: JsValue => { parse(v) }
        })
      })
    )

    val formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    private def specialMongoJson(json: JsObject) :Either[JsObject, Object] = {
      if(json.fields.length > 0) {
        json.fields(0) match {
          case (k, v :JsString) if k == "$date" => Right(
            try{
              if(v.value.toLowerCase.contains("t")){
                formater.parse(v.value)
              }else{
                new Date(v.value.toLong)
              }
            }catch{
              case e: Exception => ISODateTimeFormat.dateTime().parseDateTime(v.value).toDate()
            }
          )
          case (k, v :JsNumber) if k == "$date" => Right(new Date(v.value.toLong))
          case (k, v :JsString) if k == "$oid" => Right(new ObjectId( v.value ))
          case (k, v) if k.startsWith("$") => throw new RuntimeException("unsupported specialMongoJson " + k + " with v: " + v.getClass + ":" + v.toString())
          case _ => Left(json)
        }
      } else Left(json)

    }

    private def parse(array: JsArray) :BasicDBList = {
      val r = new BasicDBList()
      r.addAll(scala.collection.JavaConversions.seqAsJavaList(array.value map { v =>
        parse(v).asInstanceOf[Object]
      }))
      r
    }

    private def parse(v: JsValue) :Any = v match {
      case v: JsObject => parse(v)
      case v: JsArray => parse(v)
      case v: JsString => v.value
      case v: JsNumber => v.value
      case v: JsBoolean => v.value
      case JsNull => null
      case _ => null
    }
  }

  implicit object ObjectIdFormat extends Format[ObjectId] {
    def reads(json: JsValue) :JsResult[ObjectId] = JsSuccess({
      json match {
        case obj: JsObject if obj.keys.contains("$oid") => new ObjectId( (obj \ "$oid").toString )
        case s: JsString => new ObjectId(s.value)
        case _ => throw new RuntimeException("unsupported ObjectId " + json)
      }
    })
    def writes(objectId: ObjectId) :JsObject = {
      JsObject(Seq("$oid" -> JsString(objectId.toString)))
    }
  }


  implicit object MongoDateFormat extends Format[Date] {
    def reads(json: JsValue) :JsResult[Date] = JsSuccess(json match {
      case obj: JsObject if obj.keys.contains("$date") => new Date((obj \ "$date").toString.toLong)
      case _ => throw new RuntimeException("unsupported Date " + json)
    })
    def writes(date: Date) :JsObject = JsObject( Seq("$date" -> JsString(date.getTime + "")) )
  }
}
