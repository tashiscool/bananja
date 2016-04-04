package commons.ws

import org.joda.time.DateTime

object DrunkrApiHeaders {
  sealed abstract class Header[T](val name: String, val value: T)

  case class ApiKey(override val value: String) extends Header[String]("drunkr-apikey", value)
  case class CorrelationId(override val value: String) extends Header[String]("drunkr-correlationid", value)
  case class Client(override val value: String) extends Header[String]("drunkr-client", value)
  case class Region(override val value: String) extends Header[String]("drunkr-region", value)
  case class Channel(override val value: String) extends Header[String]("drunkr-channel", value)
  case class LineOfBusiness(override val value: String) extends Header[String]("drunkr-lineofbusiness", value)
  case class EndUser(override val value: String) extends Header[String]("drunkr-enduser", value)
  case class EndUserDomain(override val value: String) extends Header[String]("drunkr-enduserdomain", value)
  case class Accept(override val value: String) extends Header[String]("Accept", value)
  case class AcceptLanguage(override val value: String) extends Header[String]("Accept-language", value)
  case class Timestamp(override val value: DateTime) extends Header[DateTime]("drunkr-timestamp", value)
  case class ContentType(override val value: String) extends Header[String]("Content-Type", value)

  def toHeaderSeq(headers: Seq[Header[_]]): Seq[(String, String)] =
    headers.map( x => x.name -> x.value ).map {
      case (name, value: String) => name -> value
      case (name, date: DateTime) => name -> date.toString()
    }
}
