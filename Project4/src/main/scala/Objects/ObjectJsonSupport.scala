package Objects

import Objects.ObjectTypes.ListType
import Server.Messages.ResponseMessage
import Server.Actors.DebugInfo
import spray.httpx.SprayJsonSupport
import spray.json._

object ObjectJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  def setToJsArray(setObj: scala.collection.mutable.Set[Int]) = JsArray(setObj.map(JsNumber(_)).toVector)


  implicit object BaseObjectJsonFormat extends RootJsonFormat[BaseObject] {
    def write(bs: BaseObject) = JsObject("id" -> JsNumber(bs.id),
      "likes" -> setToJsArray(bs.likes))

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
      "users" -> JsNumber(da.users),
      "pages" -> JsNumber(da.pages),
      "posts" -> JsNumber(da.posts),
      "albums" -> JsNumber(da.albums),
      "pictures" -> JsNumber(da.pictures),
      "friendlistUpdates" -> JsNumber(da.friendlistUpdates),
      "requestPersecond" ->
        JsNumber((da.profiles + da.posts + da.albums + da.friendlistUpdates) * 1000000000.0 / (System.nanoTime() - da.start))
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("profiles", "users", "pages", "posts", "albums", "pictures", "friendlistUpdates") match {
        case Seq(JsNumber(profiles),JsNumber(users), JsNumber(pages), JsNumber(posts), JsNumber(albums),JsNumber(pictures), JsNumber(friendlistUpdates)) =>
          DebugInfo(profiles.toInt, users.toInt, pages.toInt, posts.toInt, albums.toInt, pictures.toInt, friendlistUpdates.toInt)
        case _ => throw new DeserializationException("Debug Actor expected")
      }
    }
  }

  implicit object AlbumJsonFormat extends RootJsonFormat[Album] {
    def write(a: Album) = JsObject("b" -> a.baseObject.toJson,
      "from" -> JsNumber(a.from),
      "createdTime" -> JsString(a.createdTime),
      "updatedTime" -> JsString(a.updatedTime),
      "coverPhoto" -> JsNumber(a.coverPhoto),
      "description" -> JsString(a.description),
      "pictures" -> setToJsArray(a.pictures)
    )

    def read(json: JsValue) = json.asJsObject.
      getFields("b", "from", "createdTime", "updatedTime", "coverPhoto", "description", "pictures") match {
      case Seq(b, JsNumber(from), JsString(cTime), JsString(uTime), JsNumber(cInt), JsString(desc), JsArray(pics)) =>
        val a = Album(b.convertTo[BaseObject], from.toInt, cTime, uTime, cInt.toInt, desc)
        pics.foreach { pic => a.pictures.add(pic.convertTo[Int]) }
        a
      case _ => throw new DeserializationException("Failed to deser Album")
    }
  }

  implicit val PostJsonFormat = jsonFormat7(Post)
  implicit val ResponseMessageJsonFormat = jsonFormat1(ResponseMessage)
  implicit val UpdateFriendListJsonFormat = jsonFormat3(UpdateFriendList)
  implicit val FriendListJsonFormat = jsonFormat3(FriendList)
  implicit val PageJsonFormat = jsonFormat4(Page)
  implicit val PictureJsonFormat = jsonFormat5(Picture)
  implicit val UserJsonFormat = jsonFormat6(User)

}

