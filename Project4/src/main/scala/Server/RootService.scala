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
      path("user" / IntNumber) { pid =>
        path("feed") { rc =>
          /*TODO*/
        } ~
          path("post" / IntNumber) { postId => rc => GetMsg(rc, pid, ("post", postId)) } ~
          path("albums" / IntNumber) { aId => rc => dActor(pid) ! GetMsg(rc, pid, ("album", aId)) } ~
          path("pictures" / IntNumber) { piId => rc => dActor(pid) ! GetMsg(rc, pid, ("picture", piId)) } ~
          path("post") { rc => dActor(pid) ! GetMsg(rc, pid, ("post", -1)) } ~
          path("albums") { rc => dActor(pid) ! GetMsg(rc, pid, ("album", -1)) } ~
          path("pictures") { rc => dActor(pid) ! GetMsg(rc, pid, ("picture", -1)) } ~ { rc =>
          dActor(pid) ! GetMsg(rc, pid, None)
        }
      } ~
        path("page" / IntNumber) { pid =>
          path("feed") { rc =>
            /*TODO*/
          } ~
            path("post" / IntNumber) { postId => rc => GetMsg(rc, pid, ("post", postId)) } ~
            path("albums" / IntNumber) { aId => rc => dActor(pid) ! GetMsg(rc, pid, ("album", aId)) } ~
            path("pictures" / IntNumber) { piId => rc => dActor(pid) ! GetMsg(rc, pid, ("picture", piId)) } ~
            path("post") { rc => dActor(pid) ! GetMsg(rc, pid, ("post", -1)) } ~
            path("albums") { rc => dActor(pid) ! GetMsg(rc, pid, ("album", -1)) } ~
            path("pictures") { rc => dActor(pid) ! GetMsg(rc, pid, ("picture", -1)) } ~ { rc =>
            dActor(pid) ! GetMsg(rc, pid, None)
          }
        } ~
        path("picture" / IntNumber / IntNumber) { (pid, pictureId) => rc =>
          dActor(pid) ! GetMsg(rc, pid, ("picture", pictureId))
        } ~
        path("album" / IntNumber / IntNumber) { (pid, albumId) => rc =>
          dActor(pid) ! GetMsg(rc, pid, ("album", albumId))
        } ~
        path("post" / IntNumber / IntNumber) { (pid, postId) => rc =>
          dActor(pid) ! GetMsg(rc, pid, ("post", postId))
        } ~
        path("debug") { rc => rc.complete(debugInfo) }
    } ~
      put {
        path("user") {
          entity(as[User]) { user => rc =>
            user.b.updateId(debugInfo.profiles)
            debugInfo.profiles += 1
            dActor(user.b.id) ! CreateMsg[User](rc, user)
          }
        } ~
          path("page") {
            entity(as[Page]) { page => rc =>
              page.b.updateId(debugInfo.profiles)
              debugInfo.profiles += 1
              dActor(page.b.id) ! CreateMsg[Page](rc, page)
            }
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
              debugInfo.posts += 1
              dActor(post.creator) ! CreateMsg[Post](rc, post)
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              debugInfo.albums += 1
              dActor(album.from) ! CreateMsg[Album](rc, album)
            }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc => dActor(pic.from) ! CreateMsg[Picture](rc, pic) }
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
            dActor(updFL.pid) ! UpdateMsg(rc, updFL)
          }
        } ~
          path("user") {
            entity(as[User]) { user => rc => dActor(user.b.id) ! UpdateMsg(rc, user) }
          } ~
          path("page") {
            entity(as[Page]) { page => rc => dActor(page.b.id) ! UpdateMsg(rc, page) }
          } ~
          path("post") {
            entity(as[Post]) { post => rc => dActor(post.b.id) ! UpdateMsg(rc, post) }
          } ~
          path("album") {
            entity(as[Album]) { album => rc => dActor(album.b.id) ! UpdateMsg(rc, album) }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc => dActor(pic.b.id) ! UpdateMsg(rc, pic) }

          }
      }
  }
}
