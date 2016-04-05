package bananja.drunkr.models

import commons.model.{ModelMapper, ModelLike}
import play.api.Logger
import play.api.libs.json.{JsString, JsValue}

/**
  * Created by tkhan
  */
case class SimpleData(value: String) extends ModelLike[SimpleData] {     //Should this be a class, or should the Response model have a string/single value version?
override def uuid: String = ""
}

object SimpleData {

  implicit object SimpleDataJsonMapper extends ModelMapper[SimpleData, JsValue] {
    override def apply(model: SimpleData): Option[JsValue] = Some(JsString(model.value))
  }

  implicit val mapper: ModelMapper[(String,SimpleData),JsValue] = new ModelMapper[(String,SimpleData),JsValue]{
    override def apply(from: (String, SimpleData)): Option[JsValue] = {
      Logger.debug(s"what are we doing with ${from._1}")
      Some(JsString(from._2.value))
    }
  }

}
