package bananja.drunkr.services

import play.api.mvc.RequestHeader

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
