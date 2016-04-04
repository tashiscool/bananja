package commons.mvc

import play.api.libs.Crypto
import play.api.mvc.{Cookie, DiscardingCookie, RequestHeader}
import play.api.{Application, Logger}

sealed trait AuthenticationState
case class Authenticated(userId: String) extends AuthenticationState
case class TimedOut(userId: String) extends AuthenticationState
case object Unauthenticated extends AuthenticationState

object AuthenticationHelper {
  lazy val SESSION_KEY = "sessionId"


  def maxIdleTime(implicit app: Application): Long = app.configuration.getLong("auth.max.idle.milliseconds").getOrElse(1800000)
  def rememberDuration(implicit app: Application): Int = app.configuration.getInt("auth.remember.duration").getOrElse((30 * 24 * 60 * 60))

  /**
    * Generate auth cookie for provided user
    *
    * @param user
    * @param remember Should cookie persist beyong current session. If true, cookie max age will be set to current time + MAX_DURATION
    * @return Auth cookie
    */
  def authCookie(user: UserLike, remember: Boolean = false)(implicit app: Application): Cookie = {
    val timeout = System.currentTimeMillis() + maxIdleTime
    val maxAge = if(remember) Some(rememberDuration) else None
    Cookie(s"auth", Crypto.encryptAES(s"${user.id}|$timeout"), maxAge)
  }

  def authDiscardingCookie(): DiscardingCookie = DiscardingCookie(s"auth")

  /**
    * Check for auth cookie and parse out User ID (if auth cookie is present and formatted correctly)
    *
    * @param rh Request headers
    * @return Some(userId) if valid auth cookie is present, None otherwisee
    */
  def readAuthCookie(rh: RequestHeader)(implicit app: Application): AuthenticationState = {
    rh.cookies.get(s"auth") match {
      case None =>
        Unauthenticated
      case Some(cookie) =>
        try {
          Crypto.decryptAES(cookie.value).split("\\|").toList match {
            case List(userId, timeout) =>
              if(System.currentTimeMillis() < timeout.toLong) Authenticated(userId) else TimedOut(userId)
            case _ =>
              Unauthenticated
          }
        } catch {
          case e: NumberFormatException =>
            Logger.error(s"Invalid timestamp in auth token [${e.getClass.getName}] - ${e.getMessage}")
            Unauthenticated
          case e: Throwable =>
            Logger.error(s"Error parsing auth token: [${e.getClass.getName}] - ${e.getMessage}")
            Unauthenticated
        }
    }
  }

}
