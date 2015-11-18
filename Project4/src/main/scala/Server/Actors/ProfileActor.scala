package Server.Actors

import Objects.Post
import Server.Messages.{CreateAlbum, CreatePost}
import akka.actor.{Actor, ActorRef}

import Objects.ObjectJsonSupport._
import spray.json._
import scala.collection.mutable

class ProfileActor(val debugActor: ActorRef) extends Actor {
  var numPosts = 0
  var albums = 0
  var numFriendLists = 0
  val posts = mutable.ArrayBuffer[Post]()
  //  val friendLists = mutable.ArrayBuffer[FriendList]()

  def receive = {
    case CreatePost(rc, p) =>
      p.b.updateId(numPosts)
      numPosts += 1
      posts.append(p)
      debugActor ! CreatePost
      rc.complete(p)
    case CreateAlbum(rc, a) =>
      //TODO: create instance of album actor using profileId and album id
      a.b.updateId(albums)
      albums += 1
      debugActor ! CreateAlbum
      rc.complete(a)
    //    case fl: FriendList =>
    //      fl.updateId(numFriendLists)
    //      numFriendLists += 1
    //      friendLists.append(fl)
    case _ =>
  }
}