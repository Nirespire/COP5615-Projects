package Objects.ObjectTypes

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

object ListType extends Enumeration {
  type ListType = Value
  val close_friends, acquaintances, restricted, user_created, education, work, current_city, family = Value


  implicit object ListTypeJsonFormat extends RootJsonFormat[ListType.ListType] {
    def write(obj: ListType.ListType): JsValue = JsString(obj.toString)

    def read(json: JsValue): ListType.ListType = json match {
      case JsString(str) => ListType.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }
}
