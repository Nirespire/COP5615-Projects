package core

import akka.actor.ActorRef

case class FingerEntry(var start: Int, var node: Int, var nodeRef: ActorRef) {
  def update(uNode: Int = node, uNodeRef: ActorRef = nodeRef, uStart: Int = start) = {
    start = uStart
    node = uNode
    nodeRef = uNodeRef
  }


  override def toString() = s"FE::${start}(${node})"
}