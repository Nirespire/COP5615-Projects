import akka.actor.ActorRef

// nodeId = id + 2^i
// successorId = id of node that is successor to that node id
// successor is pointer to successor of that node id
case class FingerEntry(start: Int, node: Int, nodeRef: ActorRef) {
  def updateSuccessor(nid: Int, id: Int, s: ActorRef) = {
    FingerEntry(nid, id, s)
  }

  def updateSuccessor(id: Int, s: ActorRef) = {
    FingerEntry(start, id, s)
  }

  override def toString() = s"FE::${start}(${node})"
}