package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.{Album, Post}
import Server.Messages.{GetMsg, ResponseMessage, CreateMsg}
import akka.actor.{Actor, ActorRef}

import scala.collection.mutable

class ProfileActor(val debugActor: ActorRef) extends Actor {
  var albums = mutable.ArrayBuffer[Album]()
  val posts = mutable.ArrayBuffer[Post]()
  val otherPosts = mutable.ArrayBuffer[(Int, Int)]()
  //  val friendLists = mutable.ArrayBuffer[FriendList]()

  def receive = {
    case CreateMsg(rc, obj) =>
      try {
        obj match {
          case p: Post =>
            p.b.updateId(posts.size)
            posts.append(p)
            rc.complete(p)
          case a: Album =>
            a.b.updateId(albums.size)
            albums.append(a)
            rc.complete(a)
        }
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
      }
    case GetMsg(rc, obj) =>
      try {
        obj match {
          case ("post", _, pId: Int) => if (pId == -1) rc.complete(posts.last) else rc.complete(posts(pId))
          case ("album", _, aId: Int) => if (aId == -1) rc.complete(albums.last) else rc.complete(albums(aId))
        }
      }

    case _ =>
  }
}