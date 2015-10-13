import akka.actor.{ActorRef, Props, ActorSystem}

import scala.collection.mutable
import scala.util.Random

object project3 extends App {

  val numNodes = args(0).toInt
  val numRequests = args(1).toInt

  val m = 10
  val system = ActorSystem(name = "ChordSimulation")
  var nodeHash = Random.nextInt(1024)
  val firstNode = system.actorOf(Props(new Node(m = m, id = nodeHash)), name = s"Node${nodeHash}")
  val createdNodes = mutable.ArrayBuffer[ActorRef](firstNode)
  (0 until numNodes - 1).foreach { idx =>
    val knownNodeIdx = Random.nextInt(createdNodes.size)
    while (createdNodes.contains(nodeHash)) {
      nodeHash = Random.nextInt(1024)
    }

    val knownNode = createdNodes(knownNodeIdx)
    val newNode = system.actorOf(Props(new Node(knownNode = knownNode, id = nodeHash)), name = s"Node${nodeHash}")
    createdNodes.append(newNode)
  }


}