import akka.actor.{Props, Actor, ActorRef}

import scala.collection.mutable
import scala.util.Random

class Manager(hashSpace: Int, m: Int, numNodes: Int) extends Actor {
  val createdNodes = mutable.ArrayBuffer[ActorRef]()
  var numNodesDone = 0
  var createdNodeCnt = 0

  def receive = {
    case nodeHash: Int => {
      if (createdNodeCnt == numNodesDone) {

        println("Manager is trying to create nodeHash : " + nodeHash)
        // First node in the ring
        val newNode = if (createdNodes.isEmpty) {

          //debug
          println("Manager: creating initial node")

          val n = context.actorOf(Props(new Node(self, hashSpace = hashSpace, m = m, id = nodeHash)), name = s"Node$nodeHash")
          n ! Message.InitialNode
          n
        }
        // New node joining the ring
        else {

          //debug
          println("Manager: creating node")

          val knownNodeIdx = Random.nextInt(createdNodes.size)
          val knownNode = createdNodes(knownNodeIdx)
          val newNode = context.actorOf(Props(new Node(self, hashSpace = hashSpace, m = m, id = nodeHash)), name = s"Node$nodeHash")
          newNode ! knownNode
          newNode
        }

        createdNodes.append(newNode)
        createdNodeCnt = createdNodeCnt + 1
      } else {
        self ! nodeHash
      }
    }

    case Message.Done => {
      numNodesDone = numNodesDone + 1
      if (numNodesDone == numNodes) {
        Thread.sleep(1000)
        context.system.shutdown()
      }
    }
  }
}
