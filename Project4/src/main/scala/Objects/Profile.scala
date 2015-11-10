package Objects

import spray.httpx.SprayJsonSupport
import spray.json.{JsValue, RootJsonFormat, DefaultJsonProtocol}


abstract class Profile {
  def id: Integer

  override def toString(): String ={
    "Profile id " + id
  }
}

//object PostJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
//
//  implicit val userFormat = jsonFormat6(User)
//  implicit val pageFormat = jsonFormat5(Page)
//
//  implicit val animalFormat = new RootJsonFormat[Profile] {
//    def write(obj: Profile) = obj match {
//      case x: Page => userFormat.write(x)
//      case x: User => pageFormat.write(x)
//    }
//  }
//}
