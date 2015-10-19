import akka.actor.{ActorSystem, Props}

import scala.collection.mutable
import scala.util.Random

object project3 extends App {
  val numNodes = 4
  //args(0).toInt
  val numRequests = args(1).toInt

  val m = 10
  val hashSpace = Math.pow(2, m).toInt
  val system = ActorSystem(name = "Chord")

  val manager = system.actorOf(Props(new Manager(hashSpace = hashSpace, m = m, numNodes = numNodes)), name = "manager")

  /*
  // Create the first node
  var nodeHash = Random.nextInt(hashSpace)
  manager ! nodeHash

  val createdNodesSet = mutable.Set[Int](nodeHash)
  (0 until numNodes - 1).foreach { idx =>
    createdNodesSet.add(nodeHash)
    while (createdNodesSet.contains(nodeHash)) {
      nodeHash = Random.nextInt(1024)
    }
    manager ! nodeHash
  }
  */
  List(80, 641, 585, 0).foreach(nodeHash =>
    manager ! nodeHash
  )
}