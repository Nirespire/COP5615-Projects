package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.{BaseObject, Picture, Album, Post}
import Server.Messages._
import akka.actor.{Actor, ActorRef}
import org.joda.time.DateTime
import spray.json.{JsArray, JsNumber}

import scala.collection.mutable

class ProfileActor(val pid: Int, val debugActor: ActorRef) extends Actor {
  var baseObject: BaseObject = _
  val createdTime = new DateTime().toString()
  val defaultAlbum = Album(BaseObject(0), pid, createdTime, createdTime, -1, "Default Album")
  val albums = mutable.ArrayBuffer[Album](defaultAlbum)
  val posts = mutable.ArrayBuffer[Post]()
  val otherPosts = mutable.ArrayBuffer[(Int, Int)]()
  val pictures = mutable.ArrayBuffer[Picture]()
  //  val friendLists = mutable.ArrayBuffer[FriendList]()

  def receive = {
    case CreateMsg(rc, pid, obj) =>
      try {
        if (baseObject.deleted) {
          rc.complete(ResponseMessage("Profile already deleted!"))
        } else {
          obj match {
            case p: Post =>
              p.baseObject.updateId(posts.size)
              posts.append(p)
              rc.complete(p)
            case a: Album =>
              a.baseObject.updateId(albums.size)
              albums.append(a)
              rc.complete(a)
            case p: Picture =>
              if (p.album == -1 || p.album > albums.size) p.album = 0
              albums(p.album).addPicture(p.baseObject.id)
              if (albums(p.album).coverPhoto == -1) albums(p.album).coverPhoto = p.baseObject.id
              p.baseObject.updateId(pictures.size)
              pictures.append(p)
              rc.complete(p)
          }
        }
      } catch {
        case e: Throwable => rc.complete(e.getMessage)
      }
    case getMsg@GetMsg(rc, _, obj) =>
      try {
        if (baseObject.deleted) {
          rc.complete(ResponseMessage("Profile already deleted!"))
        } else {
          obj match {
            case ("post", pId: Int) =>
              if (pId == -1) {
                val lastPostId = posts.lastIndexWhere(p => !p.baseObject.deleted)
                rc.complete(posts(lastPostId))
              } else {
                val post = posts(pId)
                if (post.baseObject.deleted) {
                  rc.complete(ResponseMessage(s"Post $pId Already deleted!"))
                } else {
                  rc.complete(post)
                }
              }
            case ("album", aId: Int) =>
              if (aId == -1) {
                val lastPostId = albums.lastIndexWhere(p => !p.baseObject.deleted)
                rc.complete(albums(lastPostId))
              } else {
                val album = albums(aId)
                if (album.baseObject.deleted) {
                  rc.complete(ResponseMessage(s"Album $aId already deleted"))
                } else {
                  rc.complete(album)
                }
              }
            case ("picture", aId: Int) =>
              if (aId == -1) {
                val lastPostId = pictures.lastIndexWhere(p => !p.baseObject.deleted)
                rc.complete(pictures(lastPostId))
              } else {
                val pic = pictures(aId)
                if (pic.baseObject.deleted) {
                  rc.complete(ResponseMessage(s"Picture $aId already deleted"))
                } else {
                  rc.complete(pic)
                }
              }

            case ("feed", fId: Int) =>
              val pIds = mutable.ArrayBuffer[Int]()
              var tenPostsCount = 10
              var postsIdx = posts.size - 1
              while (tenPostsCount > 0 && postsIdx > 0) {
                if (!posts(postsIdx).baseObject.deleted) {
                  pIds.append(postsIdx)
                }
                postsIdx -= 1
                tenPostsCount -= 1
              }
              if (tenPostsCount == 10) {
                rc.complete(ResponseMessage("No posts for this Profile!"))
              } else {
                rc.complete(JsArray(pIds.map(idx => JsNumber(idx)).toVector))
              }
          }
        }
      } catch {
        case e: Throwable => rc.complete(e.getMessage)
      }
    case deleteMsg@DeleteMsg(rc, _, obj) =>
      try {
        if (baseObject.deleted) {
          rc.complete(ResponseMessage("Profile already deleted!"))
        } else {
          obj match {
            case ("post", pId: Int) =>
              posts(pId).baseObject.delete(rc, s"Post $pId")
            case ("album", aId: Int) =>
              albums(aId).baseObject.delete(rc, s"Album $aId")
            case ("picture", pId: Int) =>
              val picture = pictures(pId)
              val album = albums(picture.album)
              album.pictures.remove(pId)
              if (album.coverPhoto == pId) album.coverPhoto = -1
              pictures(pId).baseObject.delete(rc, s"Picture $pId")
          }
        }
      } catch {
        case e: Throwable => rc.complete(e.getMessage)
      }
    case likeMsg@LikeMsg(rc, _, fid, obj) =>
      try {
        if (baseObject.deleted) {
          rc.complete(ResponseMessage("Profile already deleted!"))
        } else {
          obj match {
            case ("post", pId: Int) =>
              if (pId == -1) {
                val lastPostId = posts.lastIndexWhere(p => !p.baseObject.deleted)
                posts(lastPostId).baseObject.appendLike(fid)
                rc.complete(posts(lastPostId))
              } else {
                val post = posts(pId)
                if (post.baseObject.deleted) {
                  rc.complete(ResponseMessage(s"Post $pId Already deleted!"))
                } else {
                  post.baseObject.appendLike(fid)
                  rc.complete(post)
                }
              }
            case ("album", aId: Int) =>
              if (aId == -1) {
                val lastPostId = albums.lastIndexWhere(p => !p.baseObject.deleted)
                albums(lastPostId).baseObject.appendLike(fid)
                rc.complete(albums(lastPostId))
              } else {
                val album = albums(aId)
                if (album.baseObject.deleted) {
                  rc.complete(ResponseMessage(s"Album $aId already deleted"))
                } else {
                  album.baseObject.appendLike(fid)
                  rc.complete(album)
                }
              }
            case ("picture", aId: Int) =>
              if (aId == -1) {
                val lastPostId = pictures.lastIndexWhere(p => !p.baseObject.deleted)
                pictures(lastPostId).baseObject.appendLike(fid)
                rc.complete(pictures(lastPostId))
              } else {
                val pic = pictures(aId)
                if (pic.baseObject.deleted) {
                  rc.complete(ResponseMessage(s"Picture $aId already deleted"))
                } else {
                  pic.baseObject.appendLike(fid)
                  rc.complete(pic)
                }
              }
          }
        }
      } catch {
        case e: Throwable => rc.complete(e.getMessage)
      }
    case _ =>
  }
}