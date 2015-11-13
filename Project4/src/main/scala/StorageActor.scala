import Messages._
import Objects.Post
import akka.actor.Actor
import spray.json._
import scala.collection.mutable
import Objects.PostJsonSupport._
import Messages.StorageErrorJsonSupport._

class StorageActor extends Actor {

  var posts = mutable.HashMap[Int,Post]()

  def receive = {
    case GetPost(rc, id) =>
      if(posts.contains(id)){
        rc.complete(posts.get(id).get.toJson.compactPrint)
      }
      else{
        rc.complete(StorageError("post does not exist").toJson.compactPrint)
      }

    case CreatePost(rc,post) =>
      posts.put(post.id, post)
      rc.complete(post.toJson.compactPrint)

    case UpdatePost(rc,post) =>
      if(posts.contains(post.id)) {
        posts.remove(post.id)
        posts.put(post.id, post)
        rc.complete(post.toJson.compactPrint)
      }
      else{
        rc.complete(StorageError("post does not exist").toJson.compactPrint)
      }

    case DeletePost(rc,id) =>
      if(posts.contains(id)){
        posts.remove(id)
        rc.complete(StorageError("post deleted").toJson.compactPrint)
      }
      else{
        rc.complete(StorageError("post does not exist").toJson.compactPrint)
      }
  }
}
