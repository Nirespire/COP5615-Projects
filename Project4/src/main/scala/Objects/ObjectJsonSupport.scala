package Objects

import Objects.ObjectTypes.ListType
import Server.Messages.ResponseMessage
import Server.Actors.DebugInfo
import spray.httpx.SprayJsonSupport
import spray.json._

object ObjectJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object BaseObjectJsonFormat extends RootJsonFormat[BaseObject] {
    def write(bs: BaseObject) = JsObject("id" -> JsNumber(bs.id),
      "likes" -> JsArray(bs.likes.map(JsNumber(_)).toVector))

    def read(json: JsValue) = json.asJsObject.getFields("id", "likes") match {
      case Seq(JsNumber(id), JsArray(ids)) =>
        val bs = BaseObject(id.toInt)
        ids.foreach { likeId => bs.appendLike(likeId.convertTo[Int]) }
        bs
      case _ => throw new DeserializationException("Failed to deser BaseObject")
    }
  }


  implicit object DebugActorJsonFormat extends RootJsonFormat[DebugInfo] {
    def write(da: DebugInfo) = JsObject(
      "profiles" -> JsNumber(da.profiles),
      "posts" -> JsNumber(da.posts),
      "albums" -> JsNumber(da.albums),
      "friendlistUpdates" -> JsNumber(da.friendlistUpdates),
      "requestPersecond" ->
        JsNumber((da.profiles + da.posts + da.albums + da.friendlistUpdates) * 1000000000.0 / (System.nanoTime() - da.start))
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("profiles", "posts", "albums", "friendlistUpdates") match {
        case Seq(JsNumber(profiles), JsNumber(posts), JsNumber(albums), JsNumber(friendlistUpdates)) =>
          DebugInfo(profiles.toInt, posts.toInt, albums.toInt, friendlistUpdates.toInt)
        case _ => throw new DeserializationException("Debug Actor expected")
      }
    }
  }

  implicit val PostJsonFormat = jsonFormat6(Post)
  implicit val AlbumJsonFormat = jsonFormat7(Album)
  implicit val ResponseMessageJsonFormat = jsonFormat1(ResponseMessage)
  implicit val UpdateFriendListJsonFormat = jsonFormat3(UpdateFriendList)
  implicit val FriendListJsonFormat = jsonFormat3(FriendList)
  implicit val PageJsonFormat = jsonFormat4(Page)
  implicit val PictureJsonFormat = jsonFormat5(Picture)
  implicit val UserJsonFormat = jsonFormat6(User)

}

