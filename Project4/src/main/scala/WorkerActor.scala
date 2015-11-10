import Messages._
import Objects.Post
import akka.actor.{ActorRef, Actor}
import spray.routing.RequestContext
import spray.json._
import Objects.PostJsonSupport._
import Messages.StorageErrorJsonSupport._

class WorkerActor (requestContext: RequestContext, storageActor: ActorRef) extends Actor{

  def receive = {

    case GetPost(id) =>
      storageActor ! GetPost(id)

    case CreatePost(post) =>
      storageActor ! CreatePost(post)

    case UpdatePost(post) =>
      storageActor ! UpdatePost(post)

    case DeletePost(id) =>
      storageActor ! DeletePost(id)

    case post:Post =>
      if(post != null){
        requestContext.complete(post.toJson.compactPrint)
      }
      else{
        requestContext.complete("post does not exist")
      }

    case true =>
      requestContext.complete("post deleted")

    case s:StorageError =>
      requestContext.complete(s.toJson.compactPrint)


  }

}
