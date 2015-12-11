package Server.Actors

import java.security.Key

import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable

class DelegatorActor(debugActor: ActorRef, serverPublicKey: Key) extends Actor with ActorLogging {
  val profiles = mutable.HashMap[Long, ActorRef]()

  def receive = {
    case x => println(s"Unhandled in DelegatorActor  $x")
  }
}
