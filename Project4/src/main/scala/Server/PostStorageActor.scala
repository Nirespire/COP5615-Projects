package Server

import Server.Messages._
import Objects.Post
import akka.actor.Actor
import spray.json._
import Objects.ObjectJsonSupport._

import scala.collection.mutable

class PostStorageActor extends Actor {

  var posts = mutable.HashMap[Int,Post]()

  var numPosts = 0

  def receive = {
    case GetPost(rc, id) =>
      if(posts.contains(id)){
        rc.complete(posts.get(id).get.toJson.compactPrint)
      }
      else{
        rc.complete(ResponseMessage("post does not exist").toJson.compactPrint)
      }

    case CreatePost(rc,post) =>
      val newPost = Post(numPosts, post.creator,post.createdTime,post.from,post.message,post.postType)
      posts.put(numPosts, newPost)
      numPosts += 1
      rc.complete(newPost.toJson.compactPrint)

    case UpdatePost(rc,post) =>
      if(posts.contains(post.id)) {
        posts.remove(post.id)
        posts.put(post.id, post)
        rc.complete(post.toJson.compactPrint)
      }
      else{
        rc.complete(ResponseMessage("post does not exist").toJson.compactPrint)
      }

    case DeletePost(rc,id) =>
      if(posts.contains(id)){
        posts.remove(id)
        rc.complete(ResponseMessage("post deleted").toJson.compactPrint)
      }
      else{
        rc.complete(ResponseMessage("post does not exist").toJson.compactPrint)
      }
  }
}
