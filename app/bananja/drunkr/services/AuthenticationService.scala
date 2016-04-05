package bananja.drunkr.services

import commons.mvc.{AuthenticationHelper, AuthenticationState}
import play.api.mvc.RequestHeader
import play.api.Play.current

/**
  * Created by tkhan
  */
trait AuthenticationService{
  def readAuthCookie(rh: RequestHeader): AuthenticationState
}
class AuthenticationServiceImpl extends AuthenticationService{

  def readAuthCookie(rh: RequestHeader): AuthenticationState = {
    AuthenticationHelper.readAuthCookie(rh)
  }

}
