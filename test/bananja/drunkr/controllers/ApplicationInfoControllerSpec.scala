package bananja.drunkr.controllers

import bananja.drunkr.modules.FakeApplicationTrait
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.specs2.mutable.Specification
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.test.{FakeApplication, PlaySpecification, FakeRequest}
import play.api.test.Helpers._
import scala.concurrent.Future

//TODO: I cannot for the life of me make this work, someone smarter than me neeeds to take a look -tk

//@RunWith(classOf[JUnitRunner])
//class StudentGradesControllerSpec extends PlaySpecification with FakeApplicationTrait {
//  "StudentGrades Controller" should {
//    "return all" in new ControllerContext {
////      running(fakeApp){
//        val gradeData = List(GradeXY("foo", "bar", List(XY(0, 0)), 0))
//        val lang = "en-us"
//        val sectionId = "test"
//        val userId = "someId"
//        val someRequest = FakeRequest()
//        val requestWithContext = RequestWithContext(someRequest,Lang(lang),Some(User(userId,userId, ServiceUser)))
//
//        ctxBuilder.build(someRequest) returns (Future.successful(Right(requestWithContext)))
//        studentService.getStudentGrade(userId, sectionId) returns (Future.successful(gradeData))
//        val response = new StudentGradesApi(studentService).getStudentGrade(lang, userId, sectionId)(someRequest)
//
//        status(response) must be equalTo OK
//        val json = contentAsJson(response)
//
//        json.toString() mustEqual ""
////      }
//    }
//
//
//  }
//}
