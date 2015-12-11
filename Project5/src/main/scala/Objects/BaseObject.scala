package Objects

import Server.Messages.ResponseMessage
import Utils.Crypto
import spray.json._
import spray.routing.RequestContext
import ObjectJsonSupport._
import scala.collection.mutable

case class BaseObject(var id: Long = -1, var deleted: Boolean = false) {
  val likes = mutable.Set[Long]()

  def updateId(newId: Long) = id = newId

  def appendLike(pid: Long) = likes.add(pid)

  def delete(rc: RequestContext, msg: String = "") = {
    if (deleted) {
      rc.complete(ResponseMessage(s"$msg Already deleted!"))
    } else {
      deleted = true
      rc.complete(ResponseMessage(s"$msg deleted"))
    }
  }
}