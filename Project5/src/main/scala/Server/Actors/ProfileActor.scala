package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.PostType
import Objects._
import Server.Messages._
import Utils.{DebugInfo, Constants}
import akka.actor.{Actor, ActorRef}
import org.joda.time.DateTime
import spray.json._
import spray.routing.RequestContext

import scala.collection.mutable

abstract class ProfileActor(val pid: Int, val debugInfo: DebugInfo) extends Actor {
  val nothingIdx = 1
  val deletedIdx = 0
  val defaultAlbumIdx = 2
  val createdTime = new DateTime().toString()
  // TODO CHECK THIS
  val albums = mutable.ArrayBuffer[Album /*SecureObject[Album]*/ ](
    Album(BaseObject(deletedIdx, Constants.trueBool), pid, "", "", -1, "Profile Deleted"),
    Album(BaseObject(nothingIdx, Constants.trueBool), pid, "", "", -1, "Album Deleted"),
    Album(BaseObject(defaultAlbumIdx), pid, createdTime, createdTime, -1, "Default Album")
    //      SecureObject[Album](BaseObject(deletedIdx, Constants.trueBool), Album(BaseObject(deletedIdx, Constants.trueBool), pid, "", "", -1, "Album Deleted").toJson.compactPrint, null),
    //      SecureObject[Album](BaseObject(deletedIdx, Constants.trueBool), Album(BaseObject(nothingIdx, Constants.trueBool), pid, "", "", -1, "Album Deleted").toJson.compactPrint, null),
    //      SecureObject[Album](BaseObject(deletedIdx, Constants.trueBool), Album(BaseObject(defaultAlbumIdx), pid, createdTime, createdTime, -1, "Default Album").toJson.compactPrint, null)
  )
  val posts = mutable.ArrayBuffer[Post](
    Post(BaseObject(deletedIdx, Constants.trueBool), pid, "", pid, "Profile Deleted", PostType.empty, -1),
    Post(BaseObject(nothingIdx), pid, "", pid, "No Posts/Post deleted", PostType.empty, -1)
  )
  //  val otherPosts = mutable.ArrayBuffer[(Int, Int)]()
  val pictures = mutable.ArrayBuffer[Picture](
    Picture(BaseObject(deletedIdx, Constants.trueBool), pid, -1, "", ""),
    Picture(BaseObject(nothingIdx), pid, -1, "", "")
  )
  //  val friendLists = mutable.ArrayBuffer[FriendList]()

  def baseObject: BaseObject

  def receive = {
    //    case CreateMsg(rc, _, obj) => create(rc, obj)
    //    case getMsg@GetMsg(rc, _, obj) => get(rc, obj)
    //    case updMsg@UpdateMsg(rc, _, obj) => update(rc, obj)
    //    case deleteMsg@DeleteMsg(rc, _, obj) => delete(rc, obj)
    //    case likeMsg@LikeMsg(rc, _, fid, obj) => like(rc, fid, obj)
    case _ =>
  }

  def create(rc: RequestContext, obj: Any) = {
    try {
      obj match {
        case p: Post =>
          if (baseObject.deleted) {
            rc.complete(posts(deletedIdx))
          } else {
            p.baseObject.updateId(posts.size)
            posts.append(p)
            rc.complete(p)
          }
        case a: Album =>
          if (baseObject.deleted) {
            rc.complete(albums(deletedIdx))
          } else {
            a.baseObject.updateId(albums.size)
            albums.append(a)
            rc.complete(a)
          }
        case p: Picture =>
          if (baseObject.deleted) {
            rc.complete(pictures(deletedIdx))
          } else {
            if (p.album == -1 || p.album > albums.size) p.album = defaultAlbumIdx
            if (albums(p.album).coverPhoto == -1) albums(p.album).coverPhoto = p.baseObject.id
            p.baseObject.updateId(pictures.size)
            pictures.append(p)
            albums(p.album).addPicture(p.baseObject.id)
            rc.complete(p)
          }
      }
    } catch {
      case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
    }
  }

  def get(rc: RequestContext, obj: Any) = {
    try {

      obj match {
        case ("post", pId: Int) =>
          if (baseObject.deleted) {
            rc.complete(posts(deletedIdx))
          } else {
            if (pId == -1) {
              val lastPostId = posts.lastIndexWhere(p => !p.baseObject.deleted)
              rc.complete(posts(lastPostId))
            } else {
              val post = posts(pId)
              if (post.baseObject.deleted) {
                rc.complete(posts(nothingIdx))
              } else {
                rc.complete(post)
              }
            }
          }
        case ("album", aId: Int) =>
          if (baseObject.deleted) {
            rc.complete(albums(deletedIdx))
          } else {
            if (aId == -1) {
              val lastPostId = albums.lastIndexWhere(p => !p.baseObject.deleted)
              rc.complete(albums(lastPostId))
            } else {
              val album = albums(aId)
              if (album.baseObject.deleted) {
                rc.complete(albums(nothingIdx))
              } else {
                rc.complete(album)
              }
            }
          }
        case ("picture", aId: Int) =>
          if (baseObject.deleted) {
            rc.complete(pictures(deletedIdx))
          } else {
            if (aId == -1) {
              val lastPostId = pictures.lastIndexWhere(p => !p.baseObject.deleted)
              rc.complete(pictures(lastPostId))
            } else {
              val pic = pictures(aId)
              if (pic.baseObject.deleted) {
                rc.complete(pictures(nothingIdx))
              } else {
                rc.complete(pic)
              }
            }
          }

        case ("feed", fId: Int) =>
          val pIds = mutable.ArrayBuffer[Int](pid)
          var postsIdx = posts.size - 1
          while (pIds.size < 11 && postsIdx > 0) {
            if (!posts(postsIdx).baseObject.deleted) {
              pIds.append(postsIdx)
            }
            postsIdx -= 1
          }

          if (pIds.size == 1) {
            pIds.append(nothingIdx)
          }
          rc.complete(JsArray(pIds.map(idx => JsNumber(idx)).toVector))
      }
    } catch {
      case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
    }
  }

  def update(rc: RequestContext, obj: Any) = {
    try {
      obj match {
        case (post: Post) =>
          if (baseObject.deleted) {
            rc.complete(posts(deletedIdx))
          } else {
            val pId = post.baseObject.id
            if (post.baseObject.deleted) {
              rc.complete(posts(nothingIdx))
            } else {
              posts(pId) = post
              rc.complete(post)
            }
          }
        case (album: Album) =>
          if (baseObject.deleted) {
            rc.complete(albums(deletedIdx))
          } else {
            val aId = album.baseObject.id
            if (album.baseObject.deleted) {
              rc.complete(albums(nothingIdx))
            } else {
              albums(aId) = album
              rc.complete(album)
            }
          }
        case (picture: Picture) =>
          if (baseObject.deleted) {
            rc.complete(pictures(deletedIdx))
          } else {
            val pId = picture.baseObject.id
            if (picture.baseObject.deleted) {
              rc.complete(pictures(nothingIdx))
            } else {
              pictures(pId) = picture
              rc.complete(picture)
            }
          }
      }
    } catch {
      case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
    }
  }

  def delete(rc: RequestContext, obj: Any) = {
    try {
      if (baseObject.deleted) {
        rc.complete(ResponseMessage("Profile already deleted!"))
      } else {
        obj match {
          case ("post", pId: Int) => posts(pId).baseObject.delete(rc, s"Post $pId")
          case ("album", aId: Int) => albums(aId).baseObject.delete(rc, s"Album $aId")
          case ("picture", pId: Int) =>
            val picture = pictures(pId)
            val album = albums(picture.album)
            album.pictures.remove(pId)
            if (album.coverPhoto == pId) album.coverPhoto = -1
            pictures(pId).baseObject.delete(rc, s"Picture $pId")
        }
      }
    } catch {
      case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
    }
  }

  def like(rc: RequestContext, fid: Int, obj: Any) = {
    try {
      obj match {
        case ("post", pId: Int) =>
          if (baseObject.deleted) {
            rc.complete(posts(deletedIdx))
          } else {
            val post = posts(pId)
            if (post.baseObject.deleted) {
              rc.complete(posts(nothingIdx))
            } else {
              post.baseObject.appendLike(fid)
              rc.complete(post)
            }
          }
        case ("album", aId: Int) =>
          if (baseObject.deleted) {
            rc.complete(albums(deletedIdx))
          } else {
            val album = albums(aId)
            if (album.baseObject.deleted) {
              rc.complete(albums(nothingIdx))
            } else {
              album.baseObject.appendLike(fid)
              rc.complete(album)
            }
          }
        case ("picture", aId: Int) =>
          if (baseObject.deleted) {
            rc.complete(pictures(deletedIdx))
          } else {
            val pic = pictures(aId)
            if (pic.baseObject.deleted) {
              rc.complete(pictures(nothingIdx))
            } else {
              pic.baseObject.appendLike(fid)
              rc.complete(pic)
            }
          }
      }
    } catch {
      case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
    }
  }
}