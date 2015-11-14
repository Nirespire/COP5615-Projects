package Objects

import spray.json._
import ObjectJsonSupport._

abstract class Profile

object ProfileJsonSupport extends DefaultJsonProtocol{
  implicit object ProfileJsonFormat extends RootJsonFormat[Profile]{
    def write(p:Profile) = p match{
      case user:User => user.toJson
      case page:Page => page.toJson
    }

    def read(value:JsValue) = value match{
      case obj:JsObject if (obj.fields.size == 6) => value.convertTo[User]
      case obj:JsObject if (obj.fields.size == 5) =>  value.convertTo[Page]
    }
  }

//  implicit val userFormat = jsonFormat6(User)
//  implicit val pageFormat = jsonFormat5(Page)
}
