import akka.actor.{ActorRef, ActorSystem, Props}
import p2p.Node

import scala.collection.mutable
import scala.util.Random
import core.{Message, Manager, CircularRing}

object project3 extends App {
  val numNodes = args(0).toInt
  val numRequests = args(1).toInt

  val m = 20
  CircularRing.setM(m)
  val system = ActorSystem(name = "Chord")
  val createdNodes = mutable.ArrayBuffer[ActorRef]()
  val createdNodesSet = mutable.Set[Int]()
  val manager = system.actorOf(Props(new Manager(numNodes = numNodes)))

  var nodeHash = Random.nextInt(CircularRing.hashSpace)

  println(numNodes + " Nodes")
  println("m = " + m)
  println("Hash space of " + CircularRing.hashSpace)

  while (createdNodes.size < numNodes) {
    while (createdNodesSet.contains(nodeHash)) {
      nodeHash = Random.nextInt(CircularRing.hashSpace)
    }
    createdNodesSet.add(nodeHash)
    //println("Manager is trying to create nodeHash : " + nodeHash)
    // First node in the ring
    val newNode = if (createdNodes.isEmpty) {

      //debug
      //println("Manager: creating initial node")

      val n = system.actorOf(Props(new Node(manager = manager, m = m, n = nodeHash, numRequests = numRequests)), name = s"Node$nodeHash")
      n ! Message.InitialNode
      n
    }
    // New node joining the ring
    else {

      //debug
      //println("Manager: creating node")

      val knownNodeIdx = Random.nextInt(createdNodes.size)
      val knownNode = createdNodes(knownNodeIdx)
      val newNode = system.actorOf(Props(new Node(manager = manager, m = m, n = nodeHash, numRequests = numRequests)), name = s"Node$nodeHash")
      newNode ! knownNode
      newNode
    }

    createdNodes.append(newNode)
    //println("GOTOSLEEP")
    Thread.sleep(100)
  }

  println("Chord ring created, nodes start querying every 1 second")
  createdNodes.foreach { a =>
    //a ! true
    a ! Message.StartQuerying
  }
}