package Server.Actors

import Objects.{Post, User}
import akka.actor.Actor

import scala.collection.mutable

class UserActor(user: User) extends Actor {
  var numPosts = 0
  val posts = mutable.ArrayBuffer[Post]()

  def receive = {
    case p: Post =>
      p.updateId(numPosts)
      numPosts += 1
      posts.append(p)
    case _ =>
  }
}
