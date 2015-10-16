import akka.actor.ActorRef

object Message{
  case class InitialNode()
  case class GetNodeSuccessor(node:ActorRef, nodeId:Int)
  case class YourSuccessor(node:ActorRef, nodeId:Int)
}

