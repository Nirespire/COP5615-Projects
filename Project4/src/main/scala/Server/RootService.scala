package Server

import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.ListType.ListType
import Objects._
import Server.Actors.{DebugInfo, DelegatorActor}
import Server.Messages._
import Utils.Constants
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
  val da = DebugInfo()

  def dActor(pid: Int) = delegatorActor(pid % split)


  def routeTypes(pid: Int, ts: String, tsId: Int, rc: RequestContext) = {
    //    val profileType = profileStr match {
    //      case "user" | "page" => true
    //      case x => rc.complete(ResponseMessage(s"Unknown profile type - $x, $pid, $ts, $tsId")); false;
    //    }

    val sendActor = ts match {
      case "user" | "page" => da.debugVar(Constants.profilesChar) += 1; true;
      case "post" => da.debugVar(Constants.getPostsChar) += 1; true;
      case "album" => da.debugVar(Constants.getAlbumsChar) += 1; true;
      case "picture" => da.debugVar(Constants.getPicturesChar) += 1; true;
      case "feed" => true
      case "friendlist" => da.debugVar(Constants.getFlChar) += 1; true;
      case _ => rc.complete(ResponseMessage("Unimplemented request")); false;
    }

    if (sendActor) dActor(pid) ! GetMsg(rc, pid, (ts, tsId))
  }

  val myRoute = respondWithMediaType(`application/json`) {
    get {
      //      path(Segment / IntNumber / Segment / IntNumber) { (u, pid, ts, tsId) => rc => routeTypes(u, pid, ts, tsId, rc) } ~
      //        path(Segment / IntNumber / Segment) { (u, pid, ts) => rc => routeTypes(u, pid, ts, -1, rc) } ~
      //        path(Segment / IntNumber) { (u, pid) => rc => routeTypes(u, pid, "", -1, rc) } ~
      path(Segment / IntNumber / IntNumber) { (ts, pid, tsId) =>
        entity(as[ListType]) { listType => rc =>
          dActor(pid) ! GetMsg(rc, pid, listType)
        } ~ { rc => routeTypes(pid, ts, tsId, rc) }
      } ~
        path(Segment / IntNumber) { (ts, pid) => rc => routeTypes(pid, ts, -1, rc) } ~
        path("debug") { rc => rc.complete(da) }
    } ~
      put {
        path("like" / IntNumber / Segment / IntNumber / IntNumber) { (pid, ts, pId, fid) => rc =>
          dActor(pid) ! LikeMsg(rc, pid, fid, (ts, pId))
        } ~
          path("user") {
            entity(as[User]) { user => rc =>
              user.baseObject.updateId(da.debugVar(Constants.profilesChar))
              da.debugVar(Constants.profilesChar) += 1
              dActor(user.baseObject.id) ! CreateMsg[User](rc, user.baseObject.id, user)
            }
          } ~
          path("page") {
            entity(as[Page]) { page => rc =>
              page.baseObject.updateId(da.debugVar(Constants.profilesChar))
              da.debugVar(Constants.profilesChar) += 1
              dActor(page.baseObject.id) ! CreateMsg[Page](rc, page.baseObject.id, page)
            }
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
              da.debugVar(Constants.postsChar) += 1
              dActor(post.creator) ! CreateMsg[Post](rc, post.creator, post)
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              da.debugVar(Constants.albumsChar) += 1
              dActor(album.from) ! CreateMsg[Album](rc, album.from, album)
            }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc =>
              da.debugVar(Constants.picturesChar) += 1
              dActor(pic.from) ! CreateMsg[Picture](rc, pic.from, pic)
            }
          }
      } ~
      delete {
        path("user") {
          entity(as[User]) { user => rc =>
            dActor(user.baseObject.id) ! DeleteMsg(rc, user.baseObject.id, None)
          }
        } ~
          path("page") {
            entity(as[Page]) { page => rc =>
              dActor(page.baseObject.id) ! DeleteMsg(rc, page.baseObject.id, None)
            }
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
              dActor(post.creator) ! DeleteMsg(rc, post.creator, ("post", post.baseObject.id))
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              dActor(album.from) ! DeleteMsg(rc, album.from, ("album", album.baseObject.id))
            }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc =>
              dActor(pic.from) ! DeleteMsg(rc, pic.from, ("picture", pic.baseObject.id))
            }
          }
      } ~
      post {
        path("addfriend") {
          entity(as[UpdateFriendList]) { updFL => rc =>
            da.debugVar(Constants.flChar) += 1
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