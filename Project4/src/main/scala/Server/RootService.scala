package Server

import Objects.ObjectTypes.PostType
import Objects.{Post, User}
import Objects.ObjectJsonSupport._
import Server.Actors.UserActor
import Server.Messages._
import akka.actor.Props
import akka.util.Timeout
import org.joda.time.DateTime
import spray.http.MediaTypes.`application/json`
import spray.routing._

import scala.concurrent.duration._

trait RootService extends HttpService {

  val storage = actorRefFactory.actorOf(Props[PostStorageActor], "storage")

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)

  val storageService = actorRefFactory.actorOf(Props(new PostStorageActor()))

  val myRoute =
    respondWithMediaType(`application/json`) {
      put {
        path("user") {
          parameters('id, 'about, 'birthday, 'gender, 'firstname, 'lastname) {
            (id, about, birthday, gender, firstname, lastname) =>
              requestContext =>
                try {
                  val user = User(id.toInt, about, birthday, gender.last, firstname, lastname)
                  actorRefFactory.actorOf(Props(new UserActor(user)), name = s"user$id")
                  requestContext.complete(ResponseMessage(s"user $id created").toJson.compactPrint)
                } catch {
                  case e: Throwable =>
                    requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
                }
          }
        } ~
          path("post") {
            parameters('creator, 'from, 'message, 'ps) {
              (creator, from, message, ps) =>
                requestContext =>
                  try {
                    actorRefFactory.actorSelection(s"user$creator") ! Post(-1, new DateTime().toString, from.toInt, message, PostType.withName(ps))
                    requestContext.complete(ResponseMessage(s"Added post to $creator").toJson.compactPrint)
                  } catch {
                    case e: Throwable =>
                      requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
                  }
            }
          }
      }
      /*
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
      */
    }
}