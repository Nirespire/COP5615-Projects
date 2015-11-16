package Server

import Objects.ObjectJsonSupport._
import Objects.{Album, Post, User}
import Server.Actors.UserActor
import Server.Messages._
import akka.actor.Props
import akka.util.Timeout
import spray.http.MediaTypes.`application/json`
import spray.json._
import spray.routing._

import scala.concurrent.duration._

trait RootService extends HttpService {

  var profiles: Int = 0

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(500 milli)

  val myRoute =
    respondWithMediaType(`application/json`) {
      put {
        path("user") {
          entity(as[User]) { user =>
            requestContext =>
              try {
                user.b.updateId(profiles)
                profiles += 1
                actorRefFactory.actorOf(Props(new UserActor(user)), name = s"${user.b.id}")
                requestContext.complete(ResponseMessage(s"user ${user.b.id} created").toJson.compactPrint)
              } catch {
                case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
              }
          }
        } ~
          path("post") {
            entity(as[Post]) { post =>
              requestContext =>
                try {
                  actorRefFactory.actorSelection(s"${post.creator}") ! post
                  requestContext.complete(ResponseMessage(s"Added post to ${post.creator}").toJson.compactPrint)
                } catch {
                  case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
                }
            }
          } ~
          path("album") {
            entity(as[Album]) { album =>
              requestContext =>
                try {
                  actorRefFactory.actorSelection(s"${album.from}") ! album
                  requestContext.complete(ResponseMessage(s"Added album to ${album.from}").toJson.compactPrint)
                } catch {
                  case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
                }

            }
          }
      } ~
        get {
          path("user" / IntNumber) { pid =>
            requestContext =>
              try {
                actorRefFactory.actorSelection(s"$pid") ! GetUser(requestContext)
              } catch {
                case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
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