package commons.model

import play.api.libs.json._

/**
 * drunkr reporting Commons
 *
 * Created by tkhan
 *
 */
object EnumImplicits {
  /**
   * Produce a JSON formatter for the Enum type
   *
   * e.g. implicit val interactionLineReasonFormat = enumFormat(InteractionLineReason)
   *
   * @param ev The enclosing enum "object" to provide a formatter for that extends Enum
   * @tparam A Implied from "ev"
   * @return A JSON reader and writer format
   * @see com.drunkr.reporting.commons.model.Enum
   */
  def enumFormat[A <: Enum](ev: A): Format[A#EnumVal] =
    new Format[A#EnumVal] {
      override def reads(json: JsValue): JsResult[A#EnumVal] = {
        json match {
          case JsString(s) =>
            ev.values.find( _.name == s ).map(JsSuccess(_)).getOrElse(JsError(s"$s is not a valid InteractionType"))
          case _ =>
            JsError(s"${json.toString()} is not a valid InteractionType")
        }
      }
      override def writes(o: A#EnumVal): JsValue = JsString(o.toString())
    }
}
