package Objects

import Objects.ObjectTypes.ListType
import Server.Messages.ResponseMessage
import Server.Actors.DebugActor
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


  implicit object UpdFriendListJsonFormat extends RootJsonFormat[UpdFriendList] {
    def write(upd: UpdFriendList) = {
      JsObject("pid" -> JsNumber(upd.pid), "fid" -> JsNumber(upd.fid),
        "listType" -> JsString(upd.listType.toString))
    }

    def read(json: JsValue) = json.asJsObject.getFields("pid", "fid", "listType") match {
      case Seq(JsNumber(pid), JsNumber(fid), JsString(listType)) =>
        UpdFriendList(pid.toInt, fid.toInt, ListType.withName(listType))
      case _ => throw new DeserializationException("Failed to deser BaseObject")
    }
  }

  implicit object DebugActorJsonFormat extends RootJsonFormat[DebugActor] {
    def write(da: DebugActor) = JsObject(
      "profiles" -> JsNumber(da.profiles),
      "posts" -> JsNumber(da.posts),
      "albums" -> JsNumber(da.albums),
      "friendlistUpdates" -> JsNumber(da.friendlistUpdates)
    )

    def read(value: JsValue) ={
      value.asJsObject.getFields ("profiles", "posts", "albums", "friendlistUpdates") match {
      case Seq (JsNumber (profiles), JsNumber (posts), JsNumber (albums), JsNumber (friendlistUpdates) ) =>
      new DebugActor (profiles.toInt, posts.toInt, albums.toInt, friendlistUpdates.toInt)
      case _ => throw new DeserializationException ("Debug Actor expected")
      }
      }
    }

    implicit val PostJsonFormat = jsonFormat6(Post)
    implicit val AlbumJsonFormat = jsonFormat7(Album)
    implicit val ResponseMessageJsonFormat = jsonFormat1(ResponseMessage)
    implicit val FriendListJsonFormat = jsonFormat3(FriendList)
    implicit val PageJsonFormat = jsonFormat4(Page)
    implicit val PictureJsonFormat = jsonFormat5(Picture)
    implicit val UserJsonFormat = jsonFormat6(User)
  }
