package Objects

import Objects.ObjectTypes.ListType
import Utils.Constants
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
      "post-profiles" -> JsNumber(da.debugVar(Constants.profilesChar)),
      "post-posts" -> JsNumber(da.debugVar(Constants.postsChar)),
      "post-albums" -> JsNumber(da.debugVar(Constants.albumsChar)),
      "post-pictures" -> JsNumber(da.debugVar(Constants.picturesChar)),
      "post-friendlistUpdates" -> JsNumber(da.debugVar(Constants.flChar)),
      "post-requestPersecond" -> JsNumber(da.postRequestPerSecond()),
      "get-profiles" -> JsNumber(da.debugVar(Constants.profilesChar)),
      "get-posts" -> JsNumber(da.debugVar(Constants.postsChar)),
      "get-albums" -> JsNumber(da.debugVar(Constants.albumsChar)),
      "get-pictures" -> JsNumber(da.debugVar(Constants.picturesChar)),
      "get-friendlistUpdates" -> JsNumber(da.debugVar(Constants.flChar)),
      "get-requestPersecond" -> JsNumber(da.getRequestPerSecond()),
      "all-requestPersecond" -> JsNumber(da.allRequestPerSecond())
    )

    def read(value: JsValue) = {
      val da = DebugInfo()
      value.asJsObject.getFields("post-profiles", "post-posts", "post-albums", "post-pictures",
        "post-friendlistUpdates", "get-profiles", "get-posts", "get-albums",
        "get-pictures", "get-friendlistUpdates") match {
        case Seq(JsNumber(post_profiles),
        JsNumber(post_posts),
        JsNumber(post_albums),
        JsNumber(post_pictures),
        JsNumber(post_friendlistUpdates),
        JsNumber(get_profiles),
        JsNumber(get_posts),
        JsNumber(get_albums),
        JsNumber(get_pictures),
        JsNumber(get_friendlistUpdates)) =>
          da.debugVar(Constants.profilesChar) = post_profiles.toInt
          da.debugVar(Constants.postsChar) = post_posts.toInt
          da.debugVar(Constants.albumsChar) = post_albums.toInt
          da.debugVar(Constants.picturesChar) = post_pictures.toInt
          da.debugVar(Constants.flChar) = post_friendlistUpdates.toInt
          da.debugVar(Constants.profilesChar) = get_profiles.toInt
          da.debugVar(Constants.postsChar) = get_posts.toInt
          da.debugVar(Constants.albumsChar) = get_albums.toInt
          da.debugVar(Constants.picturesChar) = get_pictures.toInt
          da.debugVar(Constants.flChar) = get_friendlistUpdates.toInt
          da
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

