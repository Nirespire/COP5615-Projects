package Objects

import spray.httpx.SprayJsonSupport
import spray.json._


case class Picture(
               id:Int,
               from:Int,
               album:Int,
               filename:String
             )

object PictureJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PictureJsonFormat = jsonFormat4(Picture)
}