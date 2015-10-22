package core

import akka.actor.ActorRef

object Message {

  case class InitialNode()

  case class GetSuccessor(nodeId: Int)

  case class GetFingerPredecessor(id: Int, s: Int, i: Int)

  case class UpdateFingerEntry(s: Int, i: Int)

  case class UpdateFingerEntries(n: Int)

  case class UpdateFingerPredecessor(key: Int, s: Int, i: Int)

  case class UpdatePredecessor(i: Int)

  case class GetFingerSuccessor(idx: Int, nodeId: Int)

  case class YourSuccessor(id: Int, predecessor: FingerEntry)

  case class YourPredecessor(id: Int)

  case class YourFingerSuccessor(nRef: ActorRef, n: Int, i: Int)

  case class ForwardToPredecessor(id: Int, i: Int)

  case class SetupDone()

  case class StartQuerying()

  case class QueryMessage(queryVal: Int, numHops: Int)

  case class DoneQueryMessage(numHops: Int)

  case class QueryingDone(nodeId: Int, avgNumHops: Int)

}
