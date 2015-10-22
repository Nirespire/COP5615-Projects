import akka.actor.{Props, Actor, ActorRef}

import scala.collection.mutable
import scala.util.Random

class Manager(hashSpace: Int, m: Int, numNodes: Int, numRequests: Int) extends Actor {
  val createdNodes = mutable.ArrayBuffer[ActorRef]()
  var numNodesSetupDone = 0
  var createdNodeCnt = 0
  var numNodesQueryingDone = 0
  var totalAvgNumHops = 0

  def receive = {
    case nodeHash: Int => {
      if (createdNodeCnt == numNodesSetupDone) {

        println("Manager is trying to create nodeHash : " + nodeHash)
        // First node in the ring
        val newNode = if (createdNodes.isEmpty) {

          //debug
          println("Manager: creating initial node")

          val n = context.actorOf(Props(new OldNode(self, hashSpace = hashSpace, m = m, n = nodeHash, numRequests = numRequests)), name = s"Node$nodeHash")
          n ! Message.InitialNode
          n
        }
        // New node joining the ring
        else {

          //debug
          println("Manager: creating node")

          val knownNodeIdx = Random.nextInt(createdNodes.size)
          val knownNode = createdNodes(knownNodeIdx)
          val newNode = context.actorOf(Props(new OldNode(self, hashSpace = hashSpace, m = m, n = nodeHash, numRequests = numRequests)), name = s"Node$nodeHash")
          newNode ! knownNode
          newNode
        }

        createdNodes.append(newNode)
        createdNodeCnt = createdNodeCnt + 1
      } else {
        self ! nodeHash
      }
    }

    case Message.SetupDone => {
      numNodesSetupDone += 1
      if (numNodesSetupDone == numNodes) {
        createdNodes.foreach { node =>
          node ! true
        }

        //TODO: swap comments on 2 lines below to enable nodes to query the system
        //createdNodes ! Message.StartQuerying()
        context.system.shutdown()
      }
    }

    case Message.QueryingDone(nodeId, avgNumHops) => {
      println("Node " + nodeId + " just finished: ")
      println("Avg Num hops: " + avgNumHops)
      numNodesQueryingDone += 1
      totalAvgNumHops += avgNumHops

      if(numNodesQueryingDone == numNodes){
        println("All nodes done!")
        println("Avg num hops for all Nodes: " + totalAvgNumHops/numNodes)
        context.system.shutdown()
      }
    }
  }
}
