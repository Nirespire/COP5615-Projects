package Objects

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

  implicit object AlbumJsonFormat extends RootJsonFormat[Album] {
    def write(a: Album) = JsObject(
      "createdTime" -> JsString(a.createdTime),
      "updatedTime" -> JsString(a.updatedTime),
      "coverPhoto" -> JsNumber(a.coverPhoto),
      "description" -> JsString(a.description),
      "pictures" -> setToJsArray(a.pictures)
    )

    def read(json: JsValue) = json.asJsObject.
      getFields("createdTime", "updatedTime", "coverPhoto", "description", "pictures") match {
      case Seq(JsString(cTime), JsString(uTime), JsNumber(cInt), JsString(desc), JsArray(pics)) =>
        val a = Album(cTime, uTime, cInt.toInt, desc)
        pics.foreach { pic => a.pictures.add(pic.convertTo[Int]) }
        a
      case _ => throw new DeserializationException("Failed to deser Album")
    }
  }

  implicit val PostJsonFormat = jsonFormat4(Post)
  implicit val FriendListJsonFormat = jsonFormat2(FriendList)
  implicit val PageJsonFormat = jsonFormat4(Page)
  implicit val PictureJsonFormat = jsonFormat2(Picture)
  implicit val UserJsonFormat = jsonFormat6(User)
  implicit val SecureObjectJsonFormat = jsonFormat6(SecureObject)
  implicit val SecureServerRequestJsonFormat = jsonFormat4(SecureMessage)
  implicit val SecureRequestJsonFormat = jsonFormat4(SecureRequest)
}

