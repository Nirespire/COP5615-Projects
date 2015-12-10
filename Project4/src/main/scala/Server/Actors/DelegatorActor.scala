package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.{Page, User}
import Server.Messages._
import Utils.{Constants, Crypto}
import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import java.security.{PublicKey, Key}
import spray.http.HttpHeaders.RawHeader
import spray.http._
import spray.json._
import ContentTypes.`application/json`

import scala.collection.mutable

class DelegatorActor(debugActor: ActorRef, serverPublicKey: Key) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()
  val authTokens = mutable.HashMap[String, Int]()

  def receive = {
    case cMsg@CreateMsg(rc, pid, u: User) =>
      try {
        val authToken = Crypto.generateSHAToken(u.first_name+u.last_name)
        authTokens.put(authToken, u.baseObject.id)
        profiles.put(pid, context.actorOf(Props(new UserActor(u, debugActor, authToken))))
        profiles(pid) ! cMsg
        rc.complete(HttpResponse().withHeadersAndEntity(List(RawHeader(Constants.authTokenHeader, authToken)), HttpEntity(`application/json`,u.toJson.compactPrint)))

      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
      }
    case cMsg@CreateMsg(rc, pid, p: Page) =>
      try {
        val authToken = Crypto.generateSHAToken(p.category+p.about)
        authTokens.put(authToken, p.baseObject.id)
        profiles.put(pid, context.actorOf(Props(new PageActor(p, debugActor))))
        rc.complete(HttpResponse().withHeadersAndEntity(List(RawHeader(Constants.authTokenHeader, authToken)), HttpEntity(`application/json`,p.toJson.compactPrint)))
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
      }
    case cMsg@CreateMsg(rc, pid, obj) => profiles(pid) ! cMsg
    case getMsg@GetMsg(rc, pid, params) => profiles(pid) ! getMsg
    case updMsg@UpdateMsg(rc, pid, obj) => profiles(pid) ! updMsg
    case likeMsg@LikeMsg(rc, pid, fid, obj) => profiles(pid) ! likeMsg
    case deleteMsg@DeleteMsg(rc, pid, obj) => profiles(pid) ! deleteMsg
    case x => println(s"Unhandled in DelegatorActor  $x")
  }
}
