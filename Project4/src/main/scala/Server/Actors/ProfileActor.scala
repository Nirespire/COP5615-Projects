package Server.Actors

import Objects.{FriendList, Post}
import Server.Messages.{CreateAlbum, CreatePost}
import akka.actor.{ActorLogging, Actor}

import Objects.ObjectJsonSupport._
import spray.json._

import scala.collection.mutable

trait ProfileActor extends Actor with ActorLogging {
  var numPosts = 0
  var albums = 0
  var numFriendLists = 0
  val posts = mutable.ArrayBuffer[Post]()
  //  val friendLists = mutable.ArrayBuffer[FriendList]()

  def receive = {
    case CreatePost(rc, p) =>
      //      log.info(s"Post count = $numPosts")
      p.b.updateId(numPosts)
      numPosts += 1
      posts.append(p)
      rc.complete(p)
    case CreateAlbum(rc, a) =>
      //      log.info(s"Album count = $albums")
      //TODO: create instance of album actor using profileId and album id
      a.b.updateId(albums)
      albums += 1
      rc.complete(a)
    //    case fl: FriendList =>
    //      fl.updateId(numFriendLists)
    //      numFriendLists += 1
    //      friendLists.append(fl)
    case _ =>
  }
}