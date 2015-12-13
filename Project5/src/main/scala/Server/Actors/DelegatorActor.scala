package Server.Actors

import java.security.Key

import Objects.ObjectTypes.ObjectType
import Objects.SecureObject
import Server.Messages._
import Utils.{Constants, DebugInfo}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable

class DelegatorActor(debugInfo: DebugInfo, serverPublicKey: Key) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()

  def receive = {
    case PutSecureObjMsg(rc, secureObj: SecureObject) => ObjectType(secureObj.objectType) match {
      case ObjectType.user =>
        debugInfo.debugVar(Constants.putProfilesChar) += 1
        profiles.put(secureObj.to, context.actorOf(Props(new UserActor(secureObj, debugInfo))))
        rc.complete(secureObj.to.toString)
      case ObjectType.page =>
        debugInfo.debugVar(Constants.putProfilesChar) += 1
        profiles.put(secureObj.to, context.actorOf(Props(new PageActor(secureObj, debugInfo))))
        rc.complete(secureObj.to.toString)
      case _ => profiles(secureObj.to) ! PutSecureObjMsg(rc, secureObj)
    }
    case postMsg@PostSecureObjMsg(rc, secureObj) => profiles(secureObj.to) ! postMsg
    case delMsg@DeleteSecureObjMsg(rc, secureReq) => profiles(secureReq.to) ! delMsg
    case getMsg@GetSecureObjMsg(rc, secureReq) => profiles(secureReq.to) ! getMsg
    case x => log.error(s"Unhandled in DelegatorActor  $x")
  }
}