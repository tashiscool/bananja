package bananja.drunkr.it

import com.bananja.reporting.commons.mvc.{SetValue, CommandResponse}
import bananja.drunkr.models.{ServiceUser, User, SignInFormData, StudentGrade}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, Json}
import play.api.test.{FakeRequest, PlaySpecification}

@RunWith(classOf[JUnitRunner])
class ApplicationInfoSpec extends PlaySpecification {
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

class StudentGradeSpec extends PlaySpecification {
  "/analytics/v1/:lang/user/report/user/:userId/section/:sectionId" should {
    "get student grades xy" in new IntegrationContext {
      val response = route(FakeRequest(GET, "/analytics/v1/en-us/user/report/user/:userId/section/:sectionId"))

      response must beSome.which (status(_) == OK)
    }
  }

  "/analytics/v1/:lang/user/grade" should {
    "add student grade" in new IntegrationContext {
      implicit val studentGradeFormat = Json.format[StudentGrade]
      val response = route(FakeRequest(POST, "/analytics/v1/en-us/user/grade").withBody(studentGradeFormat.writes(StudentGrade("7","8","9","`",1,2,3,4,5,6))))

      response must beSome.which (status(_) == OK)
    }
  }
}

class UserApiSpec extends PlaySpecification {
  "/analytics/v1/:lang/auth/signin" should {
    "add student grade" in new IntegrationContext {
      implicit val signInFormatter = Json.format[SignInFormData]
      val response = route(FakeRequest(POST, "/analytics/v1/en-us/auth/signin").withBody(signInFormatter.writes(SignInFormData("username","password1"))))
      val responseJson = JsArray(Seq(SetValue("user", User("123", "username",ServiceUser))))
      response must beSome.which (status(_) == OK)

      response must beSome.which (contentAsString(_) == "[{\"cmd\":\"setValue\",\"data\":{\"key\":\"user\",\"value\":{\"id\":\"123\",\"username\":\"username\",\"role\":\"ServiceUser\"}}}]")

      response must beSome.which (contentAsJson(_) == responseJson)


    }
  }
}
