package bananja.drunkr.models

import com.bananja.reporting.commons.model.{ModelLike, ModelMapper}
import com.bananja.reporting.commons.mvc.{UserLike, UserRole}
import play.api.libs.json.{Json, JsValue}

/**
  * Created by tkhan
  */
case class User(id: String, username: String, role: UserRole) extends ModelLike[User] with UserLike {
  override def uuid: String = id
  override def hasRole(role: UserRole): Boolean = role == role
}


object User {

  implicit object UserJsonMapper extends ModelMapper[User,JsValue] {
    override def apply(model: User): Option[JsValue] = Some(Json.obj("id" -> model.id, "username" -> model.username, "role" -> model.role.toString))
  }

}

//Returned when user is Ok
case object ServiceUser extends UserRole


//TODO: remove this later
case class SignInFormData(username: String, password: String) extends ModelLike[SignInFormData] {
  def uuid: String = ""
}
