package bananja.drunkr.it


import bananja.drunkr.models.{User, SignInFormData}

import commons.mvc.{UserRole, SetValue}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, Json}
import play.api.test.{FakeRequest, PlaySpecification}

@RunWith(classOf[JUnitRunner])
class   ApplicationInfoSpec extends PlaySpecification {
  "/" should {
    "return default route" in new IntegrationContext {
      val response = route(FakeRequest(GET, "/"))

      println(status(response.get))

      response must beSome.which (status(_) == OK)
    }
  }

  "/ping" should {
    "return priced coffees" in new IntegrationContext {
      val response = route(FakeRequest(GET, "/ping"))

      response must beSome.which (status(_) == OK)
    }
  }

  "/info" should {
    "return priced coffees" in new IntegrationContext {
      val response = route(FakeRequest(GET, "/info"))

      response must beSome.which (status(_) == OK)
    }
  }
}

class UserApiSpec extends PlaySpecification {
  "/analytics/v1/:lang/auth/signin" should {
    "add student grade" in new IntegrationContext {
      implicit val signInFormatter = Json.format[SignInFormData]
      val response = route(FakeRequest(POST, "/analytics/v1/en-us/auth/signin").withBody(signInFormatter.writes(SignInFormData("username","password1"))))
      val responseJson = JsArray(Seq(SetValue("user", User("123", "username",UserRole.admin))))
      response must beSome.which (status(_) == OK)

      response must beSome.which (contentAsString(_) == "[{\"cmd\":\"setValue\",\"data\":{\"key\":\"user\",\"value\":{\"id\":\"123\",\"username\":\"username\",\"role\":\"ServiceUser\"}}}]")

      response must beSome.which (contentAsJson(_) == responseJson)


    }
  }
}
