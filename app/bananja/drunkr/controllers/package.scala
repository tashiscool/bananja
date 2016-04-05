package bananja.drunkr

import bananja.drunkr.models.{SimpleData, User}
import bananja.drunkr.services.{AuthenticationService, UserService}

import commons.mvc._
import play.api.i18n.Lang
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.{Application => PlayApp, Logger}

import scala.concurrent.Future

package object controllers {


  case class ServiceContext(val sessionId: String, lang: Option[Lang])

  implicit def makeServiceContext(implicit request: RequestHeader): ServiceContext = request match {
    case RequestWithSecureContext(rq, lang, _, _, _) => new ServiceContext(rq.session.get("sessionId").getOrElse(""), Option(lang))
    case RequestWithContext(rq, lang, _) => new ServiceContext(rq.session.get("sessionId").getOrElse(""), Option(lang))
    case r: RequestHeader => new ServiceContext(r.session.get("sessionId").getOrElse(""), None)
  }

  val defaultLang: Lang = Lang("en-us")


  /**
    * This is implicitly passed to all controller classes.
    *
    * @param userService
    * @param authService
    */
  class ServiceContextBuilder(userService: UserService, authService: AuthenticationService) extends ContextBuilder[User] {
    override def build(rh: Request[AnyContent]): Future[Either[Result, RequestWithContext[User]]] = {
      Logger.debug("In ContextBuilder.build")
      authService.readAuthCookie(rh) match {
        case TimedOut(_) | Unauthenticated =>
          Future.successful(Right(RequestWithContext[User](rh,defaultLang, None)))
        case Authenticated(userId) =>
          userService.getUser(userId).map {
            case Some(user) =>
              Right(RequestWithContext[User](rh,defaultLang, Some(user)))
            case None =>
              Right(RequestWithContext[User](rh,defaultLang, None))
          }
      }
    }


    override def buildAuthenticated(rh: Request[AnyContent]): Future[Either[Result, RequestWithSecureContext[User]]] = {
      Logger.debug("In ContextBuilder.buildAuthenticated")
      authService.readAuthCookie(rh) match {
        case authState@TimedOut(_) =>
          Logger.debug(s"getting timed out response for request: $rh");
          Future.successful(Left(CommandResponse.respond()(AuthTimedOut("auth", SimpleData("timed out")))))
        case authState@Unauthenticated =>
          Logger.debug(s"getting no auth found response for request: $rh");
          Future.successful(Left(CommandResponse.respond()(NoAuthFound("auth", SimpleData("no auth found")))))
        case authState@Authenticated(userId) =>
          userService.getUser(userId).map {
            case Some(user) =>
              //TODO refreshAuthentication goes here?
              //TODO Set rememberMe correctly
              Right(RequestWithSecureContext[User](rh,defaultLang, user, authState))
            case None =>
              Left(Results.Forbidden)
          }
      }
    }


  }

}
