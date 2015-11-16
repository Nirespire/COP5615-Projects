package Objects

import Server.Messages.{DebugMessage, ResponseMessage}
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object ObjectJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val BaseObjectJsonFormat = jsonFormat2(BaseObject)
  implicit val PostJsonFormat = jsonFormat6(Post)
  implicit val AlbumJsonFormat = jsonFormat7(Album)
  implicit val ResponseMessageJsonFormat = jsonFormat1(ResponseMessage)
  implicit val FriendListJsonFormat = jsonFormat4(FriendList)
  implicit val PageJsonFormat = jsonFormat4(Page)
  implicit val PictureJsonFormat = jsonFormat4(Picture)
  implicit val UserJsonFormat = jsonFormat6(User)
  implicit val DebugMessageJsonFormat = jsonFormat4(DebugMessage)
}
