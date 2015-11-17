package Server

import Objects.ObjectJsonSupport._
import Objects.{Album, Post, User}
import Server.Actors.DelegatorActor
import Server.Messages._
import akka.actor.Props
import akka.util.Timeout
import spray.http.MediaTypes.`application/json`
import spray.routing._

import scala.concurrent.duration._

trait RootService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(500 milli)
  val delegatorActor = actorRefFactory.actorOf(Props(new DelegatorActor()))
  val myRoute = respondWithMediaType(`application/json`) {
    put {
      path("user") {
        entity(as[User]) { user => rc => delegatorActor ! CreateUser(rc, user) }
      } ~
        path("post") {
          entity(as[Post]) { post => rc => delegatorActor ! CreatePost(rc, post) }
        } ~
        path("album") {
          entity(as[Album]) { album => rc => delegatorActor ! CreateAlbum(rc, album) }
        }
    } ~
      get {
        path("user" / IntNumber) { pid => rc => delegatorActor ! GetUser(rc, pid) } ~
          path("post" / IntNumber / IntNumber) { (pid, postId) => rc => delegatorActor ! GetPost(rc, pid, postId) } ~
          path("post" / IntNumber) { pid => rc => delegatorActor ! GetPost(rc, pid) } ~
          path("debug") { rc => delegatorActor ! GetServerInfo(rc) }
      }
  }
  /*
  // Update existing post
  path("post") {
    post {
      entity(as[Post]) { post =>
        rc =>
          storageService ! UpdatePost(rc, post)
      }
    }
  } ~
    // Create a new Post
    put {
      entity(as[Post]) { post =>
        rc =>
          storageService ! CreatePost(rc, post)
      }
    } ~
    path("post" / IntNumber) { (postId) =>
      // Get an existing post
      get {
        rc =>
          storageService ! GetPost(rc, postId)
      } ~
      // Delete existing post
      delete {
        rc =>
          storageService ! DeletePost(rc, postId)
      }
    }
  */
}