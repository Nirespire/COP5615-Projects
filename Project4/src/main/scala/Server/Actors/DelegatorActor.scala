package Server.Actors

import Objects.ObjectJsonSupport._
import Objects.{Page, User}
import Server.Messages._
import akka.actor.{ActorLogging, Actor, ActorRef, Props}

import spray.json._
import scala.collection.mutable

class DelegatorActor(debugActor: ActorRef) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()

  def receive = {
    case cMsg@CreateMsg(rc, pid, u: User) =>
      try {
        profiles.put(pid, context.actorOf(Props(new UserActor(u, debugActor))))
        rc.complete(u)
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
      }
    case cMsg@CreateMsg(rc, pid, p: Page) =>
      try {
        profiles.put(pid, context.actorOf(Props(new PageActor(p, debugActor))))
        rc.complete(p)
      } catch {
        case e: Throwable => rc.complete(ResponseMessage(e.getMessage))
      }
    case cMsg@CreateMsg(rc, pid, obj) => profiles(pid) ! cMsg
    case getMsg@GetMsg(rc, pid, params) => profiles(pid) ! getMsg
    case updMsg@UpdateMsg(rc, pid, obj) => profiles(pid) ! updMsg
    case _ =>
  }
}
