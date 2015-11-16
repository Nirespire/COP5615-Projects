package Server.Actors

import Objects.{Album, Post}
import Server.Messages.GetPost
import akka.actor.Actor

import scala.collection.mutable

trait ProfileActor extends Actor {
  var numPosts = 0
  var albums = 0
  val posts = mutable.ArrayBuffer[Post]()

  def receive = {
    case p: Post =>
      p.b.updateId(numPosts)
      numPosts += 1
      posts.append(p)
    case a: Album =>
      //TODO: create instance of album actor using profileId and album id
      a.b.updateId(albums)
      albums += 1

    case _ =>
  }
}