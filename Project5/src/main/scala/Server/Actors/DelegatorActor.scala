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

      ObjectType(secureObj.objectType) match {
        case ObjectType.user =>
          profiles.put(
            secureObj.baseObj.id,
            context.actorOf(Props(new UserActor(secureObj, debugInfo)))
          )
          log.info("user created")
          rc.complete("")
        case ObjectType.page =>
          profiles.put(
            secureObj.baseObj.id,
            context.actorOf(Props(new UserActor(secureObj, debugInfo)))
          )
          log.info("page created")
          rc.complete("")
      }


    case x => println(s"Unhandled in DelegatorActor  $x")
  }
}
