package bananja.drunkr.controllers

import bananja.drunkr.models._

import play.api.libs.json.{JsValue, Json}

class StudentGradesApi(studentGradeService: StudentGradeService)(implicit ctxBuilder: ServiceContextBuilder) extends ServiceController[User] {

  implicit val studentGradeFormat = Json.format[StudentGrade]

  implicit val studentGradeMapper: ModelMapper[StudentGrade,JsValue] = new JsMapper[StudentGrade]()
  implicit val liststudentGradeMapper: ModelMapper[List[StudentGrade],JsValue] = new JsMapper[List[StudentGrade]]()
  implicit val xYFormat = Json.format[XY]
  implicit val gradeXYFormat = Json.format[GradeXY]

  implicit val xyMapper: ModelMapper[XY,JsValue] = new JsMapper[XY]()
  implicit val gradeMapper: ModelMapper[GradeXY,JsValue] = new JsMapper[GradeXY]()
  implicit val listGradeMapper: ModelMapper[List[GradeXY],JsValue] = new JsMapper[List[GradeXY]]()

  def getStudentGrade(lang: String, userId: String, sectionId: String) = ActionWithContext("",lang) {
    implicit request =>
      studentGradeService.getStudentGrade(userId, sectionId).flatMap {
        case resp => CommandResponse.futureRespond()(SetValue("grades", resp.map( _.copy(label = s"${System.currentTimeMillis}")) ))
      }
  }

  def insertStudentGrade(lang: String) = ActionWithContext("", lang){
    implicit request =>
      request.body.asJson.map(_.as[StudentGrade]) match{
        case Some(grade)=>
          studentGradeService.insertStudentGrade(grade).flatMap{
            x => CommandResponse.futureRespond()(SetValue("grades-result", x))
          }
        case None => CommandResponse.futureRespond()(SetValue("grades-result", false))

      }

  }
}
