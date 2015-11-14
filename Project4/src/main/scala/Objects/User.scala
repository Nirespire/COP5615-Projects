package Objects

import spray.httpx.SprayJsonSupport
import spray.json._


case class User(
                 id:Int,
                 about:String,
                 birthday:String,
                 gender: Char,
                 first_name:String,
                 last_name:String
               ) extends Profile

object UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val UserJsonFormat = jsonFormat6(User)
}
