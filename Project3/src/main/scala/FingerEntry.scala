import akka.actor.ActorRef

// nodeId = id + 2^i
// successorId = id of node that is successor to that node id
// successor is pointer to successor of that node id
case class FingerEntry(nodeId:Int, successorId:Int, successor:ActorRef)
