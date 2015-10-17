import akka.actor.ActorRef

object Message {

  case class InitialNode()

  case class GetNodeSuccessor(node: ActorRef, nodeId: Int)

  case class YourSuccessor(id: Int, fingerTable: Array[FingerEntry])

  case class Done()

  case class QueryMessage(numHops: Int)

}

