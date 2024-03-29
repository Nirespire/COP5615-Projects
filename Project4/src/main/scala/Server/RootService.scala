package Server

import java.math.BigInteger
import java.security.SecureRandom

import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.ListType.ListType
import Objects._
import Server.Actors.{DebugInfo, DelegatorActor}
import Server.Messages._
import Utils.{Crypto, Base64Util, Constants}
import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import spray.http.HttpHeaders.RawHeader
import spray.http.MediaTypes.`application/json`
import spray.io.ServerSSLEngineProvider
import spray.routing._
import spray.json._
import spray.routing.authentication.UserPass

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

trait RootService extends HttpService {
  val split = 8

  implicit def executionContext = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5 seconds)

  private val random = new SecureRandom()

  private val serverKeyPair = Crypto.generateRSAKeys()

  implicit val myEngineProvider = ServerSSLEngineProvider { engine =>
    engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
    engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
    engine
  }

  val delegatorActor = Array.fill[ActorRef](split)(actorRefFactory.actorOf(Props(new DelegatorActor(null, serverKeyPair.getPublic))))
  val da = DebugInfo()

  def dActor(pid: Int) = delegatorActor(pid % split)

  val userRandomStrings = mutable.HashMap[Int, String]()
  val userPublicKeys = mutable.HashMap[Int, String]()


  def routeTypes(pid: Int, ts: String, tsId: Int, rc: RequestContext) = {
    //    val profileType = profileStr match {
    //      case "user" | "page" => true
    //      case x => rc.complete(ResponseMessage(s"Unknown profile type - $x, $pid, $ts, $tsId")); false;
    //    }

    val sendActor = ts match {
      case "user" | "page" => da.debugVar(Constants.getProfilesChar) += 1; true;
      case "post" => da.debugVar(Constants.getPostsChar) += 1; true;
      case "album" => da.debugVar(Constants.getAlbumsChar) += 1; true;
      case "picture" => da.debugVar(Constants.getPicturesChar) += 1; true;
      case "feed" => da.debugVar(Constants.getFeedChar) += 1; true
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
        path("debug") { rc => rc.complete(da.toJson.compactPrint) }
    } ~
      put {
        path("like" / IntNumber / Segment / IntNumber / IntNumber) { (pid, ts, pId, fid) => rc =>
          da.debugVar(Constants.postLikeChar) += 1
          dActor(pid) ! LikeMsg(rc, pid, fid, (ts, pId))
        } ~
        path("registerUser"){
          entity(as[User]){user =>
            user.baseObject.updateId(da.debugVar(Constants.putProfilesChar))
            da.debugVar(Constants.putProfilesChar) += 1
            // Generate random string and respond
            val randomString = new BigInteger(130, random).toString(32);
            userRandomStrings.put(user.baseObject.id, randomString)
            userPublicKeys.put(user.baseObject.id, user.publicKey)

            respondWithHeaders(List(RawHeader(Constants.randomStringHeader, randomString), RawHeader(Constants.serverPublicKeyHeader, Base64Util.encodeString(serverKeyPair.getPublic.getEncoded)))){
              complete(user)
            }
          }
        } ~
        path("registerPage"){
          entity(as[Page]){page =>
            page.baseObject.updateId(da.debugVar(Constants.putProfilesChar))
            da.debugVar(Constants.putProfilesChar) += 1
            // Generate random string and respond
            val randomString = new BigInteger(130, random).toString(32);
            userRandomStrings.put(page.baseObject.id, randomString)
            userPublicKeys.put(page.baseObject.id, page.publicKey)

            respondWithHeaders(List(RawHeader(Constants.randomStringHeader, randomString), RawHeader(Constants.serverPublicKeyHeader, Base64Util.encodeString(serverKeyPair.getPublic.getEncoded)))){
              complete(page)
            }
          }
        } ~
          path("user") {
            entity(as[User]) { user =>
              headerValueByName(Constants.signedStringHeader) {ss => rc =>
//                println("got user request")
//                println(ss)
                val userKey = Crypto.constructRSAPublicKeyFromBytes(Base64Util.decodeBinary(user.publicKey))
                val randomString = userRandomStrings(user.baseObject.id)
                if(Crypto.verifySign(userKey, Base64Util.decodeBinary(ss), Base64Util.encodeBinary(randomString))){
//                  user.baseObject.updateId(da.debugVar(Constants.putProfilesChar))
//                  da.debugVar(Constants.putProfilesChar) += 1
//                  println("Verify")
                  dActor(user.baseObject.id) ! CreateMsg[User](rc, user.baseObject.id, user)
                }
                //TODO  else case
              }
            }
          } ~
          path("page") {
            entity(as[Page]) { page =>
              headerValueByName(Constants.signedStringHeader) { ss => rc =>
                val pageKey = Crypto.constructRSAPublicKeyFromBytes(Base64Util.decodeBinary(page.publicKey))
                val randomString = userRandomStrings(page.baseObject.id)
                if(Crypto.verifySign(pageKey, Base64Util.decodeBinary(ss), Base64Util.encodeBinary(randomString))){
                  //                page.baseObject.updateId(da.debugVar(Constants.putProfilesChar))
                  //                da.debugVar(Constants.putProfilesChar) += 1
                  //                  println("Verify")
                  dActor(page.baseObject.id) ! CreateMsg[Page](rc, page.baseObject.id, page)
                }

              }
            }
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
              da.debugVar(Constants.putPostsChar) += 1
              dActor(post.creator) ! CreateMsg[Post](rc, post.creator, post)
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              da.debugVar(Constants.putAlbumsChar) += 1
              dActor(album.from) ! CreateMsg[Album](rc, album.from, album)
            }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc =>
              da.debugVar(Constants.putPicturesChar) += 1
              dActor(pic.from) ! CreateMsg[Picture](rc, pic.from, pic)
            }
          }
      } ~
      delete {
        path("user") {
          entity(as[User]) { user => rc =>
            da.debugVar(Constants.deleteUserChar) += 1
            dActor(user.baseObject.id) ! DeleteMsg(rc, user.baseObject.id, None)
          }
        } ~
          path("page") {
            entity(as[Page]) { page => rc =>
              da.debugVar(Constants.deletePageChar) += 1
              dActor(page.baseObject.id) ! DeleteMsg(rc, page.baseObject.id, None)
            }
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
              da.debugVar(Constants.deletePostChar) += 1
              dActor(post.creator) ! DeleteMsg(rc, post.creator, ("post", post.baseObject.id))
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              da.debugVar(Constants.deleteAlbumChar) += 1
              dActor(album.from) ! DeleteMsg(rc, album.from, ("album", album.baseObject.id))
            }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc =>
              da.debugVar(Constants.deletePictureChar) += 1
              dActor(pic.from) ! DeleteMsg(rc, pic.from, ("picture", pic.baseObject.id))
            }
          }
      } ~
      post {
        path("addfriend") {
          entity(as[UpdateFriendList]) { updFL => rc =>
            da.debugVar(Constants.postFlChar) += 1
            dActor(updFL.pid) ! UpdateMsg(rc, updFL.pid, updFL)
          }
        } ~
          path("user") {
            entity(as[User]) { user => rc =>
              da.debugVar(Constants.postUserChar) += 1
              dActor(user.baseObject.id) ! UpdateMsg(rc, user.baseObject.id, user)
            }
          } ~
          path("page") {
            entity(as[Page]) { page => rc =>
              da.debugVar(Constants.postPageChar) += 1
              dActor(page.baseObject.id) ! UpdateMsg(rc, page.baseObject.id, page)
            }
          } ~
          path("post") {
            entity(as[Post]) { post => rc =>
              da.debugVar(Constants.postPostChar) += 1
              dActor(post.creator) ! UpdateMsg(rc, post.creator, post)
            }
          } ~
          path("album") {
            entity(as[Album]) { album => rc =>
              da.debugVar(Constants.postAlbumChar) += 1
              dActor(album.from) ! UpdateMsg(rc, album.from, album)
            }
          } ~
          path("picture") {
            entity(as[Picture]) { pic => rc =>
              da.debugVar(Constants.postPictureChar) += 1
              dActor(pic.from) ! UpdateMsg(rc, pic.from, pic)
            }
          }
      }
  }

}