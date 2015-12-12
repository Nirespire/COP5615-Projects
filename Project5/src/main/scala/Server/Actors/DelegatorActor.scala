package Server.Actors

import java.security.Key

import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.ObjectType
import Objects.SecureObject
import Server.Messages._
import Utils.{DebugInfo, Constants, Crypto, Base64Util}
import akka.actor.{Props, Actor, ActorLogging, ActorRef}
import spray.json._

import scala.collection.mutable

class DelegatorActor(debugInfo: DebugInfo, serverPublicKey: Key) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()

  def receive = {
    case putMsg@PutEncryptedMsg(rc, pid, message, aesKey) =>
      val jsonMsg = Base64Util.decodeString(Crypto.decryptAES(message, aesKey, Constants.IV))
      val secureObj = JsonParser(jsonMsg).convertTo[SecureObject]

      ObjectType(secureObj.objectType) match {
        case ObjectType.user =>
          debugInfo.debugVar(Constants.putProfilesChar) += 1
          profiles.put(pid, context.actorOf(Props(new UserActor(secureObj, debugInfo))))
          rc.complete(pid.toString)
        case ObjectType.page =>
          debugInfo.debugVar(Constants.putProfilesChar) += 1
          profiles.put(pid, context.actorOf(Props(new PageActor(secureObj, debugInfo))))
          rc.complete(pid.toString)
        case _ => profiles(pid) ! PutSecureObjMsg(rc, secureObj)
      }
    case PostEncryptedMsg(rc, pid, message, aesKey) =>
      val jsonMsg = Base64Util.decodeString(Crypto.decryptAES(message, aesKey, Constants.IV))
      val secureObj = JsonParser(jsonMsg).convertTo[SecureObject]
      profiles(pid) ! PostSecureObjMsg(rc, secureObj)
    case DeleteEncryptedMsg(rc,pid,message,aesKey) =>
      val jsonMsg = Base64Util.decodeString(Crypto.decryptAES(message, aesKey, Constants.IV))
      val secureObj = JsonParser(jsonMsg).convertTo[SecureObject]
      profiles(pid) ! PostSecureObjMsg(rc, secureObj)
    case x => log.error(s"Unhandled in DelegatorActor  $x")
  }
}
