package Objects

import Objects.ObjectTypes.ListType.ListType
import spray.httpx.SprayJsonSupport
import spray.json._

case class FriendList(
                       id:Int,
                       owner:Int,
                       profiles:Array[Int],
                       list_type:ListType
                     )

object FriendListJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val FriendListJsonFormat = jsonFormat4(FriendList)
}