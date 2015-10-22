import akka.actor.{ActorRef, ActorSystem, Props}
import p2p.Node

import scala.collection.mutable
import scala.util.Random
import core.CircularRing

object project3 extends App {
  val numNodes = args(0).toInt
  val numRequests = args(1).toInt

  val m = 4
  CircularRing.setM(m)
  val system = ActorSystem(name = "Chord")

  /*
  val manager = system.actorOf(Props(new Manager(hashSpace = hashSpace, m = m, numNodes = numNodes, numRequests = numRequests)), name = "manager")

  // Create the first node
  var nodeHash = Random.nextInt(hashSpace)
  manager ! nodeHash

  val createdNodesSet = mutable.Set[Int](nodeHash)
  (0 until numNodes - 1).foreach { idx =>
    createdNodesSet.add(nodeHash)
    while (createdNodesSet.contains(nodeHash)) {
      nodeHash = Random.nextInt(hashSpace)
    }
    manager ! nodeHash
  }
  */
  val createdNodes = mutable.ArrayBuffer[ActorRef]()
  var numNodesDone = 0
  var createdNodeCnt = 0

  (1 to 16).foreach { i =>
    val nodeHash = i % 16
    println("Manager is trying to create nodeHash : " + nodeHash)
    // First node in the ring
    val newNode = if (createdNodes.isEmpty) {

      //debug
      println("Manager: creating initial node")

      val n = system.actorOf(Props(new Node(m = m, n = nodeHash, numRequests = numRequests)), name = s"Node$nodeHash")
      n ! Message.InitialNode
      n
    }
    // New node joining the ring
    else {

      //debug
      println("Manager: creating node")

      val knownNodeIdx = Random.nextInt(createdNodes.size)
      val knownNode = createdNodes(knownNodeIdx)
      val newNode = system.actorOf(Props(new Node(m = m, n = nodeHash, numRequests = numRequests)), name = s"Node$nodeHash")
      newNode ! knownNode
      newNode
    }

    createdNodes.append(newNode)
    println("GOTOSLEEP")
    Thread.sleep(5000)
  }


  createdNodes.foreach { a => a ! true }
}