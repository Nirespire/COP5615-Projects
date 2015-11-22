package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.{BaseObject, Picture, Album, Post}
import Server.Messages.{GetMsg, ResponseMessage, CreateMsg}
import akka.actor.{Actor, ActorRef}
import org.joda.time.DateTime

import scala.collection.mutable

class ProfileActor(val pid: Int, val debugActor: ActorRef) extends Actor {
  val createdTime = new DateTime().toString()
  val defaultAlbum = Album(BaseObject(0), pid, createdTime, createdTime, -1, "Default Album")
  var albums = mutable.ArrayBuffer[Album](defaultAlbum)
  val posts = mutable.ArrayBuffer[Post]()
  val otherPosts = mutable.ArrayBuffer[(Int, Int)]()
  val pictures = mutable.ArrayBuffer[Picture]()
  //  val friendLists = mutable.ArrayBuffer[FriendList]()

  def receive = {
    case CreateMsg(rc, pid, obj) =>
      try {
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
        }
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
      }
    case getMsg@GetMsg(rc, pid, obj) =>
      try {
        obj match {
          case ("post", pId: Int) => if (pId == -1) rc.complete(posts.last) else rc.complete(posts(pId))
          case ("album", aId: Int) => if (aId == -1) rc.complete(albums.last) else rc.complete(albums(aId))
          case ("picture", aId: Int) => if (aId == -1) rc.complete(pictures.last) else rc.complete(pictures(aId))
        }
      } catch {
        case e: Throwable => rc.failWith(e)
      }

    case _ =>
  }
}