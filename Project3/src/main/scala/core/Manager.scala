package core

import akka.actor.Actor

class Manager(numNodes: Int) extends Actor {
  var numNodesQueryingDone = 0
  var totalAvgNumHops = 0

  def receive = {
    case Message.QueryingDone(nodeId, avgNumHops) => {
      //println("Node " + nodeId + " just finished: ")
      //println("Avg Num hops: " + avgNumHops)
      numNodesQueryingDone += 1
      totalAvgNumHops += avgNumHops

      if (numNodesQueryingDone == numNodes) {
        println("All nodes done!")
        println("Avg num hops for all Nodes: " + totalAvgNumHops / numNodes)
        context.system.shutdown()
      }
    }
  }
}
