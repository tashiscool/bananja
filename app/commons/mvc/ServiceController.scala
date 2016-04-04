package commons.mvc

import scala.concurrent.{ExecutionContext, Future}

import play.api.Application
import play.api.i18n.Lang
import play.api.mvc._




trait WithSessionId {
  self: RequestHeader =>
  lazy val sessionId = self.session.get(AuthenticationHelper.SESSION_KEY).getOrElse(java.util.UUID.randomUUID().toString)
}



//TODO: add institution to contexts -tk
case class RequestWithContext[U <: UserLike](request: Request[AnyContent], lang: Lang, user: Option[U]) extends WrappedRequest(request) with WithSessionId
case class RequestWithSecureContext[U <: UserLike](request: Request[AnyContent], lang: Lang, user: U, authState: AuthenticationState, rememberMe: Boolean = false) extends WrappedRequest(request) with WithSessionId

abstract class ServiceController[U <: UserLike](implicit ctxBuilder: ContextBuilder[U], ec: ExecutionContext) extends Controller  {

  def ActionWithContext(context: String, lang: String, bodyParser: BodyParser[AnyContent] = parse.anyContent)(f: RequestWithContext[U] => Future[Result]) = Action.async {
    implicit request =>
      ctxBuilder.build(request) flatMap {
        case Left(r) =>
          Future.successful(r)
        case Right(requestContext) =>
          f(requestContext).map(ctxBuilder.addToSession(_,(AuthenticationHelper.SESSION_KEY, requestContext.sessionId)))
      }
  }

  def SecureActionWithContext(context: String, lang: String, bodyParser: BodyParser[AnyContent] = parse.anyContent)(f: RequestWithSecureContext[U] => Future[Result]) = Action.async {
    implicit request =>
      ctxBuilder.buildAuthenticated(request) flatMap {
        case Left(r) =>
          Future.successful(r)
        case Right(requestContext) =>
          f(requestContext).map(ctxBuilder.addToSession(_,(AuthenticationHelper.SESSION_KEY, requestContext.sessionId)))
      }
  }

  protected def refreshAuthentication(request: RequestWithSecureContext[U], result: Result)(implicit app: Application): Result = {
    result.withCookies(AuthenticationHelper.authCookie(request.user, request.rememberMe))
  }
}

trait ContextBuilder[U <: UserLike] {
  def addToSession(result: Result, tuple: (String, String))(implicit request: RequestHeader): Result = result.addingToSession(tuple)
  def build(request: Request[AnyContent]): Future[Either[Result, RequestWithContext[U]]]
  def buildAuthenticated(request: Request[AnyContent]): Future[Either[Result, RequestWithSecureContext[U]]]

}

trait UserLike {
  def id: String
  def hasRole(role: UserRole): Boolean
}

trait UserRole

object ControllerImplicits {

  implicit class PimpedResult(underlying: Result) {
    def authenticating(user: UserLike, remember: Boolean = false)(implicit app: Application): Result = underlying.withCookies(AuthenticationHelper.authCookie(user, remember))

    def loggingOut(implicit app: Application): Result = underlying.discardingCookies(AuthenticationHelper.authDiscardingCookie())
  }

}

