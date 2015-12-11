package Objects.ObjectTypes

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

object PostType extends Enumeration {
  type PostType = Value
  val empty,link, status, photo = Value

  implicit object PostTypeJsonFormat extends RootJsonFormat[PostType.PostType] {
    def write(obj: PostType.PostType): JsValue = JsString(obj.toString)

    def read(json: JsValue): PostType.PostType = json match {
      case JsString(str) => PostType.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

}



