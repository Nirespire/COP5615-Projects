import akka.actor.ActorRef

object Message {

  case class InitialNode()

  case class GetSuccessor(nodeId: Int)

  case class GetFingerSuccessor(idx: Int, nodeId: Int)

  case class YourSuccessor(id: Int, predecessor: FingerEntry)

  case class YourFingerSuccessor(idx: Int, id: Int)

  case class LookUpForward(key: Int, id: Int, i: Int)

  case class ForwardToPredecessor(id: Int, i: Int)

  case class UpdatePredecessor(id: Int)

  case class UpdateFingers(id: Int, i: Int)

  case class Done()

  case class QueryMessage(queryVal: Int, numHops: Int)

  case class SendQueryMessage()

}

