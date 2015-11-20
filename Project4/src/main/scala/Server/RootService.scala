package Server

import Objects.ObjectJsonSupport._
import Objects._
import Server.Actors.{DebugActor, DelegatorActor}
import Server.Messages._
import akka.actor.Props
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import spray.http.MediaTypes.`application/json`
import spray.routing._

import scala.concurrent.duration._

trait RootService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)
  val delegatorActor = actorRefFactory.actorOf(Props(new DelegatorActor(null)))
  val debugInfo = DebugActor()

  val myRoute = respondWithMediaType(`application/json`) {
    get {
      path("user" / IntNumber) { pid =>
        path("feed") { rc =>
          /*TODO*/
        } ~
          path("post" / IntNumber) { postId => rc =>/*TODO*/

          } ~
          path("post") { rc =>/*TODO*/

          } ~
          path("albums") { rc =>/*TODO*/

          } ~
          path("pictures") { rc =>/*TODO*/

          } ~
          path("friendlists") { rc =>/*TODO*/
            //                parameters('listType'){
            //                  listType =>
          } ~ {
          rc => delegatorActor ! GetUser(rc, pid)
        }
      } ~
        path("page" / IntNumber) { pid =>
          path("feed") { rc =>
            /*TODO*/
          } ~
            path("post" / IntNumber) { postId => rc =>/*TODO*/

            } ~
            path("post") { rc =>/*TODO*/

            } ~
            path("albums") { rc =>/*TODO*/

            } ~
            path("pictures") { rc =>/*TODO*/

            } ~ { rc =>/*TODO*/

          }
        } ~
        path("picture" / IntNumber / IntNumber) { (pid, pictureId) => rc => /*TODO*/} ~
        path("album" / IntNumber / IntNumber) { (pid, albumId) => rc => /*TODO*/} ~
        path("post" / IntNumber / IntNumber) { (pid, postId) => rc => /*TODO*/} ~
        path("debug") { rc => rc.complete(debugInfo) }
    } ~
      put {
        path("user") {
          entity(as[User]) { user => rc =>
            debugInfo.profiles += 1
            delegatorActor ! CreateUser(rc, user)
          }
        } ~
          path("page") {
            entity(as[Page]) { user => rc => /*TODO*/}
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
                debugInfo.posts += 1
                delegatorActor ! CreatePost(rc, post)
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              debugInfo.albums += 1
              delegatorActor ! CreateAlbum(rc, album)
            }
          } ~
          path("picture") {
            entity(as[Album]) { album => rc => /*TODO*/}
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
          entity(as[UpdFriendList]) { updFL => rc =>
            debugInfo.friendlistUpdates += 1
            updFL.updateRC(rc)
            delegatorActor ! updFL
          }
        } ~
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
      }
  }
}
