package Server

import Objects.ObjectJsonSupport._
import Objects._
import Server.Actors.{DebugInfo, DelegatorActor}
import Server.Messages._
import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import spray.http.MediaTypes.`application/json`
import spray.routing._

import scala.concurrent.duration._

trait RootService extends HttpService {
  val split = 8

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)
  val delegatorActor = Array.fill[ActorRef](split)(actorRefFactory.actorOf(Props(new DelegatorActor(null))))
  val debugInfo = DebugInfo()

  def dActor(pid: Int) = delegatorActor(pid % split)

  val myRoute = respondWithMediaType(`application/json`) {
    get {
      path("user" / IntNumber / Segment / IntNumber) { (pid, ts, postId) => rc => dActor(pid) ! GetMsg(rc, pid, (ts, postId)) } ~
        path("user" / IntNumber / Segment) { (pid, ts) => rc => dActor(pid) ! GetMsg(rc, pid, (ts, -1)) } ~
        path("user" / IntNumber) { pid => rc => dActor(pid) ! GetMsg(rc, pid, None) } ~
        path("page" / IntNumber / Segment / IntNumber) { (pid, ts, postId) => rc => dActor(pid) ! GetMsg(rc, pid, (ts, postId)) } ~
        path("page" / IntNumber / Segment) { (pid, ts) => rc => dActor(pid) ! GetMsg(rc, pid, (ts, -1)) } ~
        path("page" / IntNumber) { pid => rc => dActor(pid) ! GetMsg(rc, pid, None) } ~
        path("debug") { rc => rc.complete(debugInfo) } ~
        path(Segment / IntNumber / IntNumber) { (ts, pid, postId) => rc => dActor(pid) ! GetMsg(rc, pid, (ts, postId)) } ~
        path(Segment / IntNumber) { (ts, pid) => rc => dActor(pid) ! GetMsg(rc, pid, (ts, -1)) }
    } ~
      put {
        path("user") {
          entity(as[User]) { user => rc =>
            user.baseObject.updateId(debugInfo.profiles)
            debugInfo.users += 1
            debugInfo.profiles += 1
            dActor(user.baseObject.id) ! CreateMsg[User](rc, user.baseObject.id, user)
          }
        } ~
          path("page") {
            entity(as[Page]) { page => rc =>
              page.baseObject.updateId(debugInfo.profiles)
              debugInfo.pages += 1
              debugInfo.profiles += 1
              dActor(page.baseObject.id) ! CreateMsg[Page](rc, page.baseObject.id, page)
            }
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
              debugInfo.posts += 1
              dActor(post.creator) ! CreateMsg[Post](rc, post.creator, post)
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              debugInfo.albums += 1
              dActor(album.from) ! CreateMsg[Album](rc, album.from, album)
            }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc =>
              debugInfo.pictures += 1
              dActor(pic.from) ! CreateMsg[Picture](rc, pic.from, pic)
            }
          }
      } ~
      delete {
        path("user") {
          entity(as[User]) { user => rc => /*TODO*/}
        } ~
          path("page") {
            entity(as[Page]) { user => rc => /*TODO*/}
          } ~
          path("post") {
            entity(as[Post]) { post => rc => /*TODO*/}
          } ~
          path("album") {
            entity(as[Album]) { album => rc => /*TODO*/}
          } ~
          path("picture") {
            entity(as[Album]) { album => rc => /*TODO*/}
          }
      } ~
      post {
        path("addfriend") {
          entity(as[UpdateFriendList]) { updFL => rc =>
            debugInfo.friendlistUpdates += 1
            dActor(updFL.pid) ! UpdateMsg(rc, updFL.pid, updFL)
          }
        } ~
          path("user") {
            entity(as[User]) { user => rc => dActor(user.baseObject.id) ! UpdateMsg(rc, user.baseObject.id, user) }
          } ~
          path("page") {
            entity(as[Page]) { page => rc => dActor(page.baseObject.id) ! UpdateMsg(rc, page.baseObject.id, page) }
          } ~
          path("post") {
            entity(as[Post]) { post => rc => dActor(post.creator) ! UpdateMsg(rc, post.creator, post) }
          } ~
          path("album") {
            entity(as[Album]) { album => rc => dActor(album.from) ! UpdateMsg(rc, album.from, album) }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc => dActor(pic.from) ! UpdateMsg(rc, pic.from, pic) }

          }
      }
  }

}
