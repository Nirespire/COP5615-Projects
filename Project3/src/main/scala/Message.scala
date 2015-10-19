import akka.actor.ActorRef

object Message {

  case class InitialNode()

  case class GetNodeSuccessor(nodeId: Int)

  case class YourSuccessor(id: Int, fingerTable: Array[FingerEntry])

  case class UpdatePredecessor(id: Int)

  case class UpdateFingers(id: Int, successorId: Int)

  case class Done()

  case class QueryMessage(queryVal: Int, numHops: Int)

  case class SendQueryMessage()

}

