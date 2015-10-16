import akka.actor.ActorRef

case class FingerEntry(node:ActorRef, nodeId:Int, successorId:Int, successor:ActorRef)
