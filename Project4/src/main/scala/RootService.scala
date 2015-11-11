import Messages.{DeletePost, UpdatePost, CreatePost, GetPost}
import Objects.Post
import akka.actor.Props
import akka.util.Timeout
import spray.routing._
import spray.http._
import MediaTypes.`application/json`
import scala.concurrent.duration._

import Objects.PostJsonSupport._

trait RootService extends HttpService {

  val storage = actorRefFactory.actorOf(Props[StorageActor], "storage")

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)

  val storageService = actorRefFactory.actorOf(Props(new StorageActor()))

  val myRoute =
    respondWithMediaType(`application/json`) {
      path("post") {
        post {
          entity(as[Post]) { post =>
            requestContext =>
              val workerService = actorRefFactory.actorOf(Props(new WorkerActor(requestContext, storageService)))
              workerService ! UpdatePost(post)
          }
        }
      } ~
        // Create a new Post
        put {
          entity(as[Post]) { post =>
            requestContext =>
              val workerService = actorRefFactory.actorOf(Props(new WorkerActor(requestContext, storageService)))
              workerService ! CreatePost(post)
          }
        } ~
      path("post" / IntNumber) { (postId) =>
        // Get an existing post
        get {
          requestContext =>
            val workerService = actorRefFactory.actorOf(Props(new WorkerActor(requestContext, storageService)))
            workerService ! GetPost(postId)
        } ~
          delete {
            requestContext =>
              val workerService = actorRefFactory.actorOf(Props(new WorkerActor(requestContext, storageService)))
              workerService ! DeletePost(postId)
          }
      }
    }


}