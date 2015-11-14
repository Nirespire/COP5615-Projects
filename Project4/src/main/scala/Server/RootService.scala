package Server

import Server.Messages.{CreatePost, DeletePost, GetPost, UpdatePost}
import Objects.ObjectJsonSupport._
import Objects.Post
import akka.actor.Props
import akka.util.Timeout
import spray.http.MediaTypes.`application/json`
import spray.http._
import spray.routing._

import scala.concurrent.duration._

trait RootService extends HttpService {

  val storage = actorRefFactory.actorOf(Props[PostStorageActor], "storage")

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)

  val storageService = actorRefFactory.actorOf(Props(new PostStorageActor()))

  val myRoute =
    respondWithMediaType(`application/json`) {
      // Update existing post
      path("post") {
        post {
          entity(as[Post]) { post =>
            requestContext =>
              storageService ! UpdatePost(requestContext, post)
          }
        }
      } ~
        // Create a new Post
        put {
          entity(as[Post]) { post =>
            requestContext =>
              storageService ! CreatePost(requestContext, post)
          }
        } ~
        path("post" / IntNumber) { (postId) =>
          // Get an existing post
          get {
            requestContext =>
              storageService ! GetPost(requestContext, postId)
          } ~
          // Delete existing post
          delete {
            requestContext =>
              storageService ! DeletePost(requestContext, postId)
          }
        }
    }


}