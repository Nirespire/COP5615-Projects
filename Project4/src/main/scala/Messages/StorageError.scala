package Messages

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol


case class StorageError(message:String)

object StorageErrorJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val StorageErrorJsonFormat = jsonFormat1(StorageError)
}