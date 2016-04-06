package bananja.drunkr.services

import bananja.drunkr.models.dao.models.dao.sapi.UserDaoReactive

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
class UserServiceImpl(userDaoReactive: UserDaoReactive) extends UserService{
  def getUser(id: String): Future[Option[User]] = userDaoReactive.getUserById(id)

}
