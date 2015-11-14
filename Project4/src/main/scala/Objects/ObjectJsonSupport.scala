package Objects

import Messages.ResponseMessage
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object ObjectJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PostJsonFormat = jsonFormat6(Post)
  implicit val AlbumJsonFormat = jsonFormat7(Album)
  implicit val ResponseMessageJsonFormat = jsonFormat1(ResponseMessage)
  implicit val FriendListJsonFormat = jsonFormat4(FriendList)
  implicit val PageJsonFormat = jsonFormat5(Page)
  implicit val PictureJsonFormat = jsonFormat4(Picture)
  implicit val UserJsonFormat = jsonFormat6(User)
}
