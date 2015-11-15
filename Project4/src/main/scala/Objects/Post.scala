package Objects

import Objects.ObjectTypes.PostType.PostType
import spray.httpx.SprayJsonSupport
import spray.json._

case class Post(
                 var id: Int,
                 creator: Int,
                 createdTime: String,
                 from: Int,
                 message: String,
                 postType: PostType
               ) {
  def updateId(newId: Int) = id = newId
}