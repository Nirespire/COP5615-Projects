package Objects

import spray.httpx.SprayJsonSupport
import spray.json._


case class Page (
                 id:Int,
                 about:String,
                 category:String,
                 cover:Int,
                 likes:Int
               ) extends Profile

object PageJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PageJsonFormat = jsonFormat5(Page)
}