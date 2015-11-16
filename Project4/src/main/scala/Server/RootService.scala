package Server

import Objects.ObjectJsonSupport._
import Objects.{FriendList, Album, Post, User}
import Server.Actors.{DebugActor, UserActor}
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

  val debugActor = actorRefFactory.actorOf(Props(new DebugActor()))

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
                debugActor ! CreateProfile
                //requestContext.complete(ResponseMessage(s"user ${user.b.id} created").toJson.compactPrint)
                requestContext.complete(user)
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
                  debugActor ! CreatePost
                  //requestContext.complete(ResponseMessage(s"Added post to ${post.creator}").toJson.compactPrint)
                  requestContext.complete(post)
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
                  debugActor ! CreateAlbum
                  //requestContext.complete(ResponseMessage(s"Added album to ${album.from}").toJson.compactPrint)
                  requestContext.complete(album)
                } catch {
                  case e: Throwable => requestContext.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
                }

            }
          }~
          path("friendlist") {
            entity(as[FriendList]) { friendlist =>
              requestContext =>
                try {
                  actorRefFactory.actorSelection(s"${friendlist.owner}") ! friendlist
                  debugActor ! CreateFriendList
                  requestContext.complete(friendlist)
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
        }~
        path("debug"){
          requestContext =>
            debugActor ! GetServerInfo(requestContext)
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