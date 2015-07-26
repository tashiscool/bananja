package models.dao.sapi

import java.io.{StringWriter, Serializable}
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.util
import java.util._

import org.apache.commons.lang3.StringUtils
import org.codehaus.jackson.map.DeserializationConfig
import org.codehaus.jackson.map.ObjectMapper
import play.Logger
import scala.collection.JavaConverters._

object AbstractModel {
  /**
   * Static helper to convert aprse a json String to the given object
   *
   *
   * @param json  JSON string to parse
   * @param clazz Object type to parse JSON too. Must be able to parse this object using Jackson
   * @param <T>   Type of object to parse from JSON
   * @return      Some(T) if object could be parsed from JSON string, None otherwise.
   */
  @SuppressWarnings(Array("unchecked")) def fromJson[T](json: String, clazz: Class[T]): Option[T] = {
    if (StringUtils.isBlank(json)) {
      return None
    }
    else {
      try {
        val mapper: ObjectMapper = new ObjectMapper
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val u: T = mapper.readValue(json, clazz)
        if (u != null) {
          return Some(u)
        }
        else {
          return None
        }
      }
      catch {
        case e: Exception => {
          Logger.error("Error parsing input " + json, e)
          return None
        }
      }
    }
  }
}

case class AbstractModel( createdDate: Date = null, lastUpdatedDate: Date = null) extends Serializable {

  def withCreatedDate(createdDate: Date): AbstractModel = {
    this.copy(createdDate = createdDate)
  }

  def withLastUpdatedDate(lastUpdatedDate: Date): AbstractModel = {
    this.copy(lastUpdatedDate = lastUpdatedDate)
  }

  /**
   * Serialize this model to a JSON object
   *
   * @return  JSON string if the object could be serialized or an empty String if there was a problem.
   */
  def toJson: String = {
    try {
      val mapper: ObjectMapper = new ObjectMapper
      val sw: StringWriter = new StringWriter
      mapper.writeValue(sw, this)
      val jsonValue: String = sw.toString
      if (jsonValue.contains("'")) {
        return jsonValue.replaceAll("'", "&#39;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
      }
      return jsonValue.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
    }
    catch {
      case e: Exception => {
        Logger.error("Error serializing User object to JSON", e)
        return ""
      }
    }
  }
}

/**
 * Created by tashdid.khan on 7/10/2015.
 */
object User {

  @SerialVersionUID(1L)
  case class UserPreferences(language: String = null, emailOptIn: Boolean = false) extends Serializable {

    def withLanguage(language: String): User.UserPreferences = {
      this.copy(language = language)
    }

    def withEmailOptIn(emailOptIn: Boolean): User.UserPreferences = {
      this.copy(emailOptIn = emailOptIn)
    }
  }


  class UserVendorMapping extends AbstractModel {
    var vendor: String = null
    var username: String = null
    var userId: String = null

    def this(vendor: String, username: String, userId: String) {
      this()
      this.vendor = vendor
      this.username = username
      this.userId = userId
    }

    override def equals(o: Any): Boolean = {
      if (this == o) return true
      if (!(o.isInstanceOf[User.UserVendorMapping])) return false
      val that: User.UserVendorMapping = o.asInstanceOf[User.UserVendorMapping]
      if (!(vendor == that.vendor)) return false
      return true
    }

    override def hashCode: Int = {
      val result: Int = vendor.hashCode
      return result
    }
  }

}

class User extends AbstractModel {
  def withExperience(expr: Option[Experience]) = {
    expr match{
      case Some(e)=> experiences.add(e);skills.addAll(e.skillsUsed.asJava); awards.addAll(e.awardswon.asJava);sources.addAll(e.sourcesReferenced.asJava);this
      case None => this
    }
  }
  def withEducation(expr: Option[Education]) = {
    expr match{
      case Some(e)=> education.add(e);skills.addAll(e.skillslearned.asJava); awards.addAll(e.awardswon.asJava);sources.addAll(e.sourcesReferenced.asJava);this
      case None => this
    }
  }

  var id: String = StringUtils.EMPTY
  var userId: String = StringUtils.EMPTY
  var partyId: String = StringUtils.EMPTY
  var username: String = StringUtils.EMPTY
  var salutation: String = StringUtils.EMPTY
  var firstName: String = StringUtils.EMPTY
  var lastName: String = StringUtils.EMPTY
  var organizationName: String = StringUtils.EMPTY
  var addressWarning: Boolean = false
  var preferences: User.UserPreferences = null
  var password: String = StringUtils.EMPTY
  var code: String = StringUtils.EMPTY
  var salt: String = StringUtils.EMPTY
  var encrypted: String = StringUtils.EMPTY
  var changeEmailAddress: String = StringUtils.EMPTY
  var changeEmailToken: String = StringUtils.EMPTY
  var usernameMapping: Set[User.UserVendorMapping] = new HashSet[User.UserVendorMapping]
  var hasBadDemographics: Boolean = false
  var contactConsent: Boolean = false
  var coached: Boolean = false

  var skills: List[Skill] = new util.ArrayList[Skill]()
  var sources: List[ExternalSource] = new util.ArrayList[ExternalSource]()
  var awards: List[Award] = new util.ArrayList[Award]()
  var education: List[Education] = new util.ArrayList[Education]()
  var experiences: List[Experience]= new util.ArrayList[Experience]()

  def withUserId(userId: String, vendor: String): User = {
    this.userId = userId
    if (StringUtils.isNotBlank(vendor)) {
      var found: Boolean = false
      import scala.collection.JavaConversions._
      for (vendorsname <- usernameMapping) {
        if (vendorsname.vendor == vendor) {
          vendorsname.userId = userId
          found = true
        }
      }
      if (!found) {
        usernameMapping.add(new User.UserVendorMapping(vendor, username, StringUtils.defaultIfBlank(userId, "")))
      }
    }
    else if (StringUtils.isNotBlank(userId) && !usernameMapping.isEmpty) {
      import scala.collection.JavaConversions._
      for (vendorsname <- usernameMapping) {
        if (StringUtils.isBlank(vendorsname.userId)) {
          vendorsname.userId = userId
        }
      }
    }
    return this
  }

  def withPartyId(partyId: String): User = {
    this.partyId = partyId
    return this
  }

  def withUsername(username: String): User = {
    this.username = username
    var forReference: String = null
    import scala.collection.JavaConversions._
    for (vendorsname <- usernameMapping) {
      if (vendorsname.username == username) {
        forReference = vendorsname.vendor
      }
    }
    if (forReference != null) {
      usernameMapping.add(new User.UserVendorMapping(forReference, username, StringUtils.defaultIfBlank(userId, "")))
    }
    return this
  }

  def withSalutation(salutation: String): User = {
    this.salutation = salutation
    return this
  }

  def withFirstName(firstName: String): User = {
    this.firstName = StringUtils.capitalize(firstName)
    return this
  }

  def withLastName(lastName: String): User = {
    this.lastName = StringUtils.capitalize(lastName)
    return this
  }

  def withOrganizationName(organizationName: String): User = {
    this.organizationName = organizationName
    return this
  }

  def withAddressWarning(addressWarning: Boolean): User = {
    this.addressWarning = addressWarning
    return this
  }

  def withUserNameMapping(nameSpace: String, username: String, userId: String): User = {
    this.usernameMapping.add(new User.UserVendorMapping(nameSpace, username, userId))
    return this
  }


  def withPreferences(preferences: User.UserPreferences): User = {
    this.preferences = preferences
    return this
  }

  def withPassword(password: String): User = {
    if (StringUtils.isNotBlank(password)) {
      salt = UUID.randomUUID.toString

      try {
        this.password = password
        encrypted = password
      }
      catch {
        case e: NoSuchAlgorithmException => {
          e.printStackTrace
        }
        case e: InvalidKeySpecException => {
          e.printStackTrace
        }
      }
    }
    return this
  }

  def withChangeEmailAddress(changeEmailAddress: String): User = {
    this.changeEmailAddress = changeEmailAddress
    return this
  }

  def withChangeEmailToken(changeEmailToken: String): User = {
    this.changeEmailToken = changeEmailToken
    return this
  }

  def getFullName: String = {
    if (StringUtils.equals(this.firstName, this.lastName)) {
      return this.firstName
    }
    else {
      return this.firstName + " " + this.lastName
    }
  }

  def hasBadDemographics(demo: Boolean): User = {
    this.hasBadDemographics = demo
    return this
  }

  def withContactConsent(demo: Boolean): User = {
    this.contactConsent = demo
    return this
  }

  def withCoached(demo: Boolean): User = {
    this.coached = demo
    return this
  }
}


