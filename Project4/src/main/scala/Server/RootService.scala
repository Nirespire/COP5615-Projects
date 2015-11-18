package Server

import Objects.ObjectJsonSupport._
import Objects.{UpdFriendList, Album, Post, User}
import Server.Actors.{DebugActor, DelegatorActor}
import Server.Messages._
import akka.actor.Props
import akka.util.Timeout
import spray.http.MediaTypes.`application/json`
import spray.routing._

import scala.concurrent.duration._

trait RootService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)
  val debugActor = actorRefFactory.actorOf(Props(new DebugActor()))
  val delegatorActor = actorRefFactory.actorOf(Props(new DelegatorActor(debugActor)))
  val myRoute = respondWithMediaType(`application/json`) {
    get {
      path("user" / IntNumber) { pid => rc => delegatorActor ! GetUser(rc, pid) } ~
        path("post" / IntNumber / IntNumber) { (pid, postId) => rc => delegatorActor ! GetPost(rc, pid, postId) } ~
        path("post" / IntNumber) { pid => rc => delegatorActor ! GetPost(rc, pid) } ~
        path("debug") { rc => debugActor ! GetServerInfo(rc) }
    } ~
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
      post {
        path("addfriend") {
          entity(as[UpdFriendList]) { updFL => rc =>
            updFL.updateRC(rc)
            delegatorActor ! updFL
          }
        }
      }
  }
}
