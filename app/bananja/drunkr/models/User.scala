package bananja.drunkr.models

import slick.driver.PostgresDriver.api._
import commons.model.{ModelLike, ModelMapper}
import commons.mvc.{UserLike, UserRole}
import play.api.libs.json.{Json, JsValue}
import commons.model.EnumImplicits.enumFormat

/**
  * Created by tkhan
  */
case class User(id: String, username: String, role: UserRole.EnumVal) extends ModelLike[User] with UserLike {
  override def uuid: String = id
  override def hasRole(role: UserRole.EnumVal): Boolean = role == role
}


object User {

  implicit object UserJsonMapper extends ModelMapper[User,JsValue] {
    override def apply(model: User): Option[JsValue] = Some(Json.obj("id" -> model.id, "username" -> model.username,
      "role" -> model.role.toString))
  }

  implicit val enumFomatter = enumFormat(UserRole)
  implicit val userFormats = Json.format[User]
}



//TODO: remove this later
case class SignInFormData(username: String, password: String) extends ModelLike[SignInFormData] {
  def uuid: String = ""
}

class UserTable(tag: Tag) extends Table[User](tag, "dim_user_table") {

  def id = column[String]("id", O.PrimaryKey)

  def username = column[String]("username")

  def role = column[String]("role")

  def customrole = (role.?).<>[UserRole.EnumVal, Option[String]](input => UserRole.fromString(input.getOrElse("")), otherPoint => Some(Some(otherPoint.name)))

  def * = (id, username, customrole ) <>((User.apply _).tupled, User.unapply)
}
