package Server.Actors

import java.security.Key

import Objects.ObjectJsonSupport._
import Objects.ObjectTypes.ObjectType
import Objects.SecureObject
import Server.Messages.PutMsg
import Utils.{DebugInfo, Constants, Crypto, Base64Util}
import akka.actor.{Props, Actor, ActorLogging, ActorRef}
import spray.json._

import scala.collection.mutable

class DelegatorActor(debugInfo: DebugInfo, serverPublicKey: Key) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()

  def receive = {
    case putMsg@PutMsg(rc, message, aesKey) =>
      val jsonMsg = Base64Util.decodeString(Crypto.decryptAES(message, aesKey, Constants.IV))
      val secureObj = JsonParser(jsonMsg).convertTo[SecureObject]
      val id = secureObj.baseObj.id

      ObjectType(secureObj.objectType) match {
        case ObjectType.user =>
          profiles.put(
            secureObj.baseObj.id,
            context.actorOf(Props(new UserActor(secureObj, debugInfo)))
          )
          log.info("user created")

        case ObjectType.page =>
          profiles.put(
            secureObj.baseObj.id,
            context.actorOf(Props(new UserActor(secureObj, debugInfo)))
          )
          log.info("page created")
        case ObjectType.post =>
        case ObjectType.picture =>
        case ObjectType.album =>
        case ObjectType.updateFriendList =>
      }
      // Should return generated ID of object to user so they can reference it later
      rc.complete(id.toString)

    case x => log.error(s"Unhandled in DelegatorActor  $x")
  }
}
