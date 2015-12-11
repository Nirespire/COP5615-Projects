package Server.Actors

import java.security.Key

import Objects.SecureObject
import Server.Messages.PutMsg
import Utils.{Constants, Crypto, Base64Util}
import akka.actor.{Actor, ActorLogging, ActorRef}
import spray.json.JsonParser

import scala.collection.mutable

class DelegatorActor(debugActor: ActorRef, serverPublicKey: Key) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Int, ActorRef]()

  def receive = {
    case putMsg@PutMsg(rc, message, aesKey) =>
      val jsonMsg = Base64Util.decodeString(Crypto.decryptAES(message, aesKey, Constants.IV))
      //      val secureObj = JsonParser(jsonMsg).convertTo[SecureObject]
      println(jsonMsg)
    case x => println(s"Unhandled in DelegatorActor  $x")
  }
}
