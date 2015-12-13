package Server.Actors

import Objects.ObjectTypes.ObjectType
import Objects._
import Server.Messages._
import Utils.{Constants, DebugInfo}
import akka.actor.{Actor, ActorLogging}
import org.joda.time.DateTime
import spray.routing.RequestContext

import scala.collection.mutable

abstract class ProfileActor(val pid: Int, val debugInfo: DebugInfo) extends Actor with ActorLogging {
  val nothingIdx = 1
  val deletedIdx = 0
  val defaultAlbumIdx = 2
  val createdTime = new DateTime().toString()

  val albums = mutable.ArrayBuffer[SecureObject](
    SecureObject(BaseObject(deletedIdx, Constants.trueBool), pid, pid, ObjectType.album.id, "".getBytes, Map()),
    SecureObject(BaseObject(nothingIdx, Constants.trueBool), pid, pid, ObjectType.album.id, "".getBytes, Map())
  )

  val posts = mutable.ArrayBuffer[SecureObject](
    SecureObject(BaseObject(deletedIdx, Constants.trueBool), pid, pid, ObjectType.post.id, "".getBytes, Map()),
    SecureObject(BaseObject(nothingIdx), pid, pid, ObjectType.post.id, "".getBytes, Map())
  )

  val pictures = mutable.ArrayBuffer[SecureObject](
    SecureObject(BaseObject(deletedIdx, Constants.trueBool), pid, pid, ObjectType.picture.id, "".getBytes, Map()),
    SecureObject(BaseObject(nothingIdx), pid, pid, ObjectType.picture.id, "".getBytes, Map())
  )

  def baseObject: BaseObject

  def receive = {
    case PutSecureObjMsg(rc, secureObj) => put(rc, secureObj)
    case PostSecureObjMsg(rc, secureObj) => post(rc, secureObj)
    case DeleteSecureObjMsg(rc, secureRequest) => delete(rc, secureRequest)
    case x => log.info(s"Unhandled case : $x")
  }

  def put(rc: RequestContext, secureObj: SecureObject) = ObjectType(secureObj.objectType) match {
    case ObjectType.post =>
      secureObj.baseObj.updateId(posts.size)
      posts.append(secureObj)
      debugInfo.debugVar(Constants.putPostsChar) += 1
      rc.complete((posts.size - 1).toString)
    case ObjectType.picture =>
      secureObj.baseObj.updateId(pictures.size)
      pictures.append(secureObj)
      debugInfo.debugVar(Constants.putPicturesChar) += 1
      rc.complete((pictures.size - 1).toString)
    case ObjectType.album =>
      secureObj.baseObj.updateId(albums.size)
      albums.append(secureObj)
      debugInfo.debugVar(Constants.putAlbumsChar) += 1
      rc.complete((albums.size - 1).toString)
    case ObjectType.updateFriendList =>
  }

  def post(rc: RequestContext, secureObj: SecureObject) = ObjectType(secureObj.objectType) match {
    case ObjectType.post =>

      val postId = secureObj.baseObj.id
      if (posts(postId).baseObj.deleted) {
        rc.complete("Post already deleted!")
      } else {
        posts(postId) = secureObj
        rc.complete("Post updated!")
      }

    case ObjectType.picture =>

      val pictureId = secureObj.baseObj.id
      if (pictures(pictureId).baseObj.deleted) {
        rc.complete("Picture already deleted!")
      } else {
        pictures(pictureId) = secureObj
        rc.complete("Picture updated!")
      }

    case ObjectType.album =>

      val albumId = secureObj.baseObj.id
      if (albums(albumId).baseObj.deleted) {
        rc.complete("Album already deleted!")
      } else {
        albums(albumId) = secureObj
        rc.complete("Album updated!")
      }

  }

  def delete(rc: RequestContext, secureReq: SecureRequest) = ObjectType(secureReq.objectType) match {
    //TODO
    case _ => rc.complete("NotImplemented")
  }
}