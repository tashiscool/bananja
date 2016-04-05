package bananja.drunkr.services

import commons.mvc.{AuthenticationHelper, AuthenticationState}
import bananja.drunkr.models.User
import commons.mvc.UserRole
import play.api.mvc.RequestHeader
import scala.concurrent.Future
import play.api.Play.current

/**
  * Created by tkhan
  */
trait UserService {
  def getUser(id: String): Future[Option[User]]
}
class UserServiceImpl extends UserService{
  def getUser(id: String): Future[Option[User]] = Future.successful(Some(User("","",UserRole.admin)))

}
