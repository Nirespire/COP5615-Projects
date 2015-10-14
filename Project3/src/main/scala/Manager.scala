import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable
import scala.util.Random

class Manager(m: Integer) extends Actor {
  val createdNodes = mutable.ArrayBuffer[ActorRef]()

  def receive = {
    case nodeHash: Int => {
      val newNode = if (createdNodes.isEmpty) {
        context.actorOf(Props(new Node(m = m, id = nodeHash)), name = s"Node$nodeHash")
      } else {
        val knownNodeIdx = Random.nextInt(createdNodes.size)
        val knownNode = createdNodes(knownNodeIdx)
        val newNode = context.actorOf(Props(new Node(m = m, id = nodeHash)), name = s"Node$nodeHash")
        newNode ! knownNode
        newNode
      }

      createdNodes.append(newNode)
    }
  }
}
