import Messages._
import Objects.Post
import akka.actor.Actor

import scala.collection.mutable

class StorageActor extends Actor {

  var posts = mutable.HashMap[Int,Post]()

  def receive = {
    case GetPost(id) =>
      if(posts.contains(id)){
        sender ! posts.get(id).get
      }
      else{
        sender ! StorageError("post does not exist")
      }

    case CreatePost(post) =>
      posts.put(post.id, post)
      sender ! post

    case UpdatePost(post) =>
      if(posts.contains(post.id)) {
        posts.remove(post.id)
        posts.put(post.id, post)
        sender ! post
      }
      else{
        sender ! StorageError("post does not exist")
      }

    case DeletePost(id) =>
      if(posts.contains(id)){
        posts.remove(id)
        sender ! true
      }
      else{
        sender ! StorageError("post does not exist")
      }
  }
}
