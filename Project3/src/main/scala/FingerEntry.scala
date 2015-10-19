import akka.actor.ActorRef

// nodeId = id + 2^i
// successorId = id of node that is successor to that node id
// successor is pointer to successor of that node id
case class FingerEntry(nodeId: Int, successorId: Int, successor: ActorRef) {
  def updateSuccessor(nid: Int, id: Int, s: ActorRef) = {
    FingerEntry(nid, id, s)
  }

  def updateSuccessor(id: Int, s: ActorRef) = {
    FingerEntry(nodeId, id, s)
  }

  override def toString() = s"FE::${nodeId}(${successorId})"
}