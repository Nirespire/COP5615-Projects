package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.{Album, Post, User, UpdateFriendList}
import Server.Messages._
import akka.actor.{ActorLogging, Actor, ActorRef, Props}

import spray.json._
import scala.collection.mutable

class DelegatorActor(debugActor: ActorRef) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()

  def receive = {
    case cMsg@CreateMsg(rc, obj) =>
      try {
        obj match {
          case u: User =>
            profiles.put(u.b.id, context.actorOf(Props(new UserActor(u, debugActor))))
            rc.complete(u)
          case p: Post => profiles(p.creator) ! cMsg
          case a: Album => profiles(a.from) ! cMsg
        }
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case getMsg@GetMsg(rc, pid, params) =>
      try {
        params match {
          case pid: Int => profiles(pid) ! rc
          case _ => profiles(getMsg.pid) ! getMsg
        }
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case updMsg@UpdateMsg(rc, obj) =>
      try {
        obj match {
          case updFL: UpdateFriendList => profiles(updFL.pid) ! updMsg
        }
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage).toJson.compactPrint)
      }
    case _ =>
  }
}
